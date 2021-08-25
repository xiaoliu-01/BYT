package com.register.byt.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.register.byt.hosp.mapper.ScheduleMapper;
import com.register.model.vo.hosp.ScheduleOrderVo;
import org.springframework.data.domain.Page;
import com.register.byt.commons.result.ResultCodeEnum;
import com.register.byt.exception.BytException;
import com.register.byt.hosp.repository.ScheduleRepository;
import com.register.byt.hosp.service.DepartmentService;
import com.register.byt.hosp.service.HospitalService;
import com.register.byt.hosp.service.ScheduleService;
import com.register.model.entity.hosp.BookingRule;
import com.register.model.entity.hosp.Department;
import com.register.model.entity.hosp.Hospital;
import com.register.model.entity.hosp.Schedule;
import com.register.model.vo.hosp.BookingScheduleRuleVo;
import com.register.model.vo.hosp.ScheduleQueryVo;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author LLXX
 * @create 2021-08-06 9:08
 */
@Slf4j
@Service
public class ScheduleServiceImpl extends ServiceImpl<ScheduleMapper,Schedule> implements ScheduleService {

    @Resource
    private ScheduleRepository scheduleRepository;

    @Resource
    private MongoTemplate mongoTemplate;

    @Resource
    private HospitalService hospitalService;

    @Resource
    private DepartmentService departmentService;

    @Override
    public void saveSchedule(Map<String, Object> paramMap) {
        String s = JSONObject.toJSONString(paramMap);
        Schedule schedule = JSONObject.parseObject(s, Schedule.class);
        String hosCode = schedule.getHoscode();
        String hosScheduleId = schedule.getHosScheduleId();
        // 查询是否已存在排班
        Schedule scheduleExist = scheduleRepository.getScheduleByHoscodeAndHosScheduleId(hosCode,hosScheduleId);
        if(scheduleExist != null){
            // 存在（则更新）
            scheduleExist.setUpdateTime(new Date());
            scheduleExist.setStatus(1);
            scheduleExist.setIsDeleted(0);
            scheduleRepository.save(scheduleExist);
        }else {
            // 不存在（添加）
            schedule.setCreateTime(new Date());
            schedule.setUpdateTime(new Date());
            schedule.setStatus(1);
            schedule.setIsDeleted(0);
            scheduleRepository.save(schedule);
        }
    }

    @Override
    public Page<Schedule> findSchedulePage(Integer page, Integer pageSize, ScheduleQueryVo queryVo) {
        Pageable pageable = PageRequest.of(page - 1,pageSize);
        Schedule schedule = new Schedule();
        BeanUtils.copyProperties(queryVo,schedule);
        schedule.setIsDeleted(0);
        ExampleMatcher matcher = ExampleMatcher.matching();
        matcher.withIgnoreCase(true).withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
        Example<Schedule> example = Example.of(schedule,matcher);
        Page<Schedule> schedulePage = scheduleRepository.findAll(example, pageable);
        return schedulePage;
    }

    @Override
    public void removeScheduleByCode(String hosCode, String hosScheduleId) {
        Schedule schedule = scheduleRepository.getScheduleByHoscodeAndHosScheduleId(hosCode, hosScheduleId);
        if(schedule != null) {
            log.warn("开始删除排班");
            scheduleRepository.deleteById(schedule.getId());
        };
    }

    @Override
    public Map<String, Object> getRuleSchedule(long page, long limit, String hosCode, String depCode) {
        // 1、根据医院编号 和 科室编号 查询
        Criteria criteria = Criteria.where("hoscode").is(hosCode).and("depcode").is(depCode);
        // 2、根据工作日workDate期进行分组
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("workDate")//分组字段
                            .first("workDate")
                            .as("workDate")
                            .count().as("docCount")  // 3、统计号源数量
                            .sum("reservedNumber").as("reservedNumber")
                            .sum("availableNumber").as("availableNumber")
                            .last("status").as("status"),
                // 排序
                //Aggregation.sort(Sort.Direction.ASC,"workDate"),
                Aggregation.sort(Sort.by("workDate")),
                // 4、实现分页
                Aggregation.skip((page - 1) * limit),
                Aggregation.limit(limit)
        );
        // 调用方法，最终执行
        AggregationResults<BookingScheduleRuleVo> bookingScheduleRuleVos =
                mongoTemplate.aggregate(aggregation, Schedule.class, BookingScheduleRuleVo.class);
        List<BookingScheduleRuleVo> scheduleRuleVos = bookingScheduleRuleVos.getMappedResults();
        log.info("scheduleRuleVos = " + scheduleRuleVos);
        // 5、分组查询的总记录数
        Aggregation totalAggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("workDate")
                           .first("workDate")
                           .as("workDate")
        );
        AggregationResults<BookingScheduleRuleVo> totalAggResults  =
                mongoTemplate.aggregate(totalAggregation, Schedule.class, BookingScheduleRuleVo.class);
        // 总记录
        int total = totalAggResults.getMappedResults().size();
        // 6、把日期对应星期获取
        scheduleRuleVos.forEach(mappedResult -> {
            Date workDate = mappedResult.getWorkDate();
            String dayOfWeek = this.getDayOfWeek(new DateTime(workDate));
            mappedResult.setDayOfWeek(dayOfWeek);
        });
        // 7、设置最终数据，进行返回
        HashMap<String, Object> resultMap = new HashMap<>();
        resultMap.put("scheduleRuleVos",scheduleRuleVos);
        resultMap.put("total",total);
        // 创建baseMap用于封装其他数据
        HashMap<String, Object> baseMap = new HashMap<>();
        String hosName = hospitalService.getHosNameByCode(hosCode);
        baseMap.put("hosName",hosName);
        resultMap.put("baseMap",baseMap);
        return resultMap;
    }

    @Override
    public List<Schedule> getDetailSchedule(String hosCode, String depCode, String workDate) {
        //根据参数查询mongodb
        List<Schedule> scheduleList =
                scheduleRepository.findScheduleByHoscodeAndDepcodeAndWorkDate(hosCode,depCode,new DateTime(workDate).toDate());
        //把得到list集合遍历，向设置其他值：医院名称、科室名称、日期对应星期
        scheduleList.forEach(schedule -> {
            packageSchedule(schedule);
        });
        return scheduleList;
    }

    @Override
    public HashMap<String, Object> getBookingScheduleRule(Integer page, Integer limit, String hosCode, String depCode) {
        // 1、根据医院ID,获取预约规则
        Hospital hospital = hospitalService.getHospitalByCode(hosCode);
        if(hospital == null){
            throw new BytException(ResultCodeEnum.DATA_ERROR);
        }
        BookingRule bookingRule = hospital.getBookingRule();
        // 2、获取可预约日期分页数据
        IPage<Date> iPage = getScheduledPageDate(page,limit,bookingRule);
        // 当前可预约日期
        List<Date> dataRecords = iPage.getRecords();
        Criteria criteria = Criteria.where("hoscode").is(hosCode).and("depcode").is(depCode).and("workDate").in(dataRecords);
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("workDate").first("workDate").as("workDate") // 分组字段
                            .count().as("docCount") // 统计号源数量
                            .sum("availableNumber").as("availableNumber")
                            .sum("reservedNumber").as("reservedNumber"),
                Aggregation.sort(Sort.by("workDate"))
        );
        // 调用方法，聚合查询
        AggregationResults<BookingScheduleRuleVo> aggregate = mongoTemplate.aggregate(aggregation, Schedule.class, BookingScheduleRuleVo.class);
        List<BookingScheduleRuleVo> bookingScheduleRuleVos = aggregate.getMappedResults();
        //获取科室剩余预约数
        //合并数据 将统计数据bookingScheduleRuleVos根据“安排日期”合并到BookingRuleVo
        Map<Date, BookingScheduleRuleVo> scheduleVoMap = new HashMap<>();
        if(!CollectionUtils.isEmpty(bookingScheduleRuleVos)){
            scheduleVoMap = bookingScheduleRuleVos.stream()
                    .collect(Collectors.toMap(BookingScheduleRuleVo::getWorkDate, BookingScheduleRuleVo  -> BookingScheduleRuleVo ));
        }
        // 获取可预约排班规则
        List<BookingScheduleRuleVo> scheduleRuleVoList = new ArrayList<>();
        for (int i = 0 , len = dataRecords.size() ; i < len ; i++) {
            Date date = dataRecords.get(i);
            BookingScheduleRuleVo bookingScheduleRuleVo = scheduleVoMap.get(date);
            if(bookingScheduleRuleVo == null){ // 说明当天没有排班医生
                bookingScheduleRuleVo = new  BookingScheduleRuleVo();
                bookingScheduleRuleVo.setDocCount(0);
                //科室可预约数  -1表示无号
                bookingScheduleRuleVo.setAvailableNumber(-1);
                // 科室剩余号数  -1表示无号
                bookingScheduleRuleVo.setReservedNumber(-1);
            }
            bookingScheduleRuleVo.setWorkDate(date);
            bookingScheduleRuleVo.setWorkDateMd(date);
            String dayOfWeek = getDayOfWeek(new DateTime(date));
            bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);
            // 最后一页,最后一条记录为即将预约   状态 0：正常  1：即将放号 -1：当天已停止挂号
            if(i == len - 1  &&  page == iPage.getPages()){
                bookingScheduleRuleVo.setStatus(1); // 即将放号
            }else {
                bookingScheduleRuleVo.setStatus(0); // 正常
            }
            // 当天预约如果过了停号时间， 不能预约
            if(i == 0 && page == 1){ // 当天（第一条排班信息）
                // 当天放号结束时间
                DateTime stopTime  = getDateTime(new Date(), bookingRule.getStopTime());
                if(stopTime.isBeforeNow()){
                    // 停止预约
                    bookingScheduleRuleVo.setStatus(-1); // 当天已停止挂号
                }
            }
            scheduleRuleVoList.add(bookingScheduleRuleVo);
        }
        // 封装数据
        HashMap<String, Object> map = new HashMap<>();
        //可预约日期规则数据
        map.put("bookingScheduleList",scheduleRuleVoList);
        map.put("total",iPage.getTotal());
        // 其他基础数据
        HashMap<String, Object> baseMap = new HashMap<>();
        //医院名称
        baseMap.put("hosname", hospitalService.getHosNameByCode(hosCode));
        //科室
        Department department = departmentService.getDepByHosCodeAndDepCode(depCode,hosCode);
        //大科室名称
        baseMap.put("bigname", department.getBigname());
        //科室名称
        baseMap.put("depname", department.getDepname());
        //月
        baseMap.put("workDateString", new DateTime().toString("yyyy年MM月"));
        //放号时间
        baseMap.put("releaseTime", bookingRule.getReleaseTime());
        //停号时间
        baseMap.put("stopTime", bookingRule.getStopTime());
        // 封装到大Map中
        map.put("baseMap",baseMap);
        return map;
    }

    @Override
    public Schedule getScheduleDetailById(String scheduleId) {
        Optional<Schedule> optionalSchedule = scheduleRepository.findById(scheduleId);
        Schedule schedule = optionalSchedule.get();
        this.packageSchedule(schedule);
        return schedule;
    }

    @Override
    public ScheduleOrderVo getScheduleOrderVo(String scheduleId) {
        ScheduleOrderVo scheduleOrderVo = new ScheduleOrderVo();
        // 获取排班详细
        Schedule schedule = scheduleRepository.findById(scheduleId).get();
        //Schedule schedule = baseMapper.selectById(scheduleId);
        if(schedule == null) throw new BytException(ResultCodeEnum.DATA_ERROR);
        // 获取医院详细
        Hospital hospital = hospitalService.getHospitalByCode(schedule.getHoscode());
        if(hospital == null) throw new BytException(ResultCodeEnum.DATA_ERROR);
        // 获取预约详细
        BookingRule bookingRule = hospital.getBookingRule();
        if(bookingRule == null) throw new BytException(ResultCodeEnum.DATA_ERROR);
        // 封装值
        scheduleOrderVo.setHoscode(schedule.getHoscode());
        scheduleOrderVo.setHosname(hospital.getHosname());
        scheduleOrderVo.setDepcode(schedule.getDepcode());
        scheduleOrderVo.setDepname(departmentService.getDepNameByCode(schedule.getDepcode(),schedule.getHoscode()));
        scheduleOrderVo.setHosScheduleId(schedule.getHosScheduleId());
        scheduleOrderVo.setAvailableNumber(schedule.getAvailableNumber());
        scheduleOrderVo.setTitle(schedule.getTitle());
        scheduleOrderVo.setReserveDate(schedule.getWorkDate());
        scheduleOrderVo.setReserveTime(schedule.getWorkTime());
        scheduleOrderVo.setAmount(schedule.getAmount());

        //退号截止天数（如：就诊前一天为-1，当天为0）
        int quitDay = bookingRule.getQuitDay();
        DateTime quitTime = this.getDateTime(new DateTime(schedule.getWorkDate()).plusDays(quitDay).toDate(), bookingRule.getQuitTime());
        scheduleOrderVo.setQuitTime(quitTime.toDate());

        //预约开始时间
        DateTime startTime = this.getDateTime(new Date(), bookingRule.getReleaseTime());
        scheduleOrderVo.setStartTime(startTime.toDate());

        //预约截止时间
        DateTime endTime = this.getDateTime(new DateTime().plusDays(bookingRule.getCycle()).toDate(), bookingRule.getStopTime());
        scheduleOrderVo.setEndTime(endTime.toDate());

        //当天停止挂号时间
        DateTime stopTime = this.getDateTime(new Date(), bookingRule.getStopTime());
        scheduleOrderVo.setStartTime(stopTime.toDate());

        return scheduleOrderVo;
    }

    @Override
    public boolean update(Schedule schedule) {
        try {
            scheduleRepository.save(schedule);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 获取可预约日期分页数据
    private IPage getScheduledPageDate(Integer page, Integer limit, BookingRule bookingRule) {
        // 当天放号时间
        DateTime releaseTime = getDateTime(new Date(),bookingRule.getReleaseTime());
        // 放号周期
        Integer cycle = bookingRule.getCycle();
        // 如果当前时间已超当天放号时间，则周期加一
        if (releaseTime.isBeforeNow()) cycle += 1;
        // 可预约所有日期，最后一天显示即将放号倒计时
        List<Date> dates = new ArrayList<>();
        for (int i = 0; i < cycle; i++) {
            // 计算当前可预约日期
            DateTime curDate = new DateTime().plusDays(i);
            String curDateTime = curDate.toString("yyyy-MM-dd");
            dates.add(new DateTime(curDateTime).toDate());
        }
        //日期分页，由于预约周期不一样，页面一排最多显示7天数据，多了就要分页显示
        List<Date> pageDateList = new ArrayList<>(); // 分页数据
        int start = (page - 1) * limit; //开始日期
        int end = (page - 1) * limit + limit; //
        if(end > dates.size()) end = dates.size();
        for (int i = start ; i < end ; i++) {
            pageDateList.add(dates.get(i));
        }
        IPage<Date> iPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page(page, 7 , dates.size());
        iPage.setRecords(pageDateList); // 设置分页数据
        return iPage;
    }

    /**
     * 将Date日期转化为DateTime
     * @param date
     * @return
     */
    private DateTime getDateTime(Date date,String timeString){
        String s = new DateTime(date).toString("yyyy-MM-dd") + " " + timeString;
        DateTime dateTime = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").parseDateTime(s);
        return dateTime;
    }

    //封装排班详情其他值 医院名称、科室名称、日期对应星期
    private void packageSchedule(Schedule schedule) {
        // 设置医院名称
        String hosName = hospitalService.getHosNameByCode(schedule.getHoscode());
        // 科室名称
        String depName = departmentService.getDepNameByCode(schedule.getDepcode(),schedule.getHoscode());
        // 日期对应星期
        String dayOfWeek = getDayOfWeek(new DateTime(schedule.getWorkDate()));
        // 封装值
        Map<String, Object> param = schedule.getParam();
        param.put("hosName",hosName);
        param.put("depName",depName);
        param.put("dayOfWeek",dayOfWeek);
        schedule.setParam(param);
    }

    /**
     * 根据日期获取周几数据
     * @param dateTime
     * @return
     */
    private String getDayOfWeek(DateTime dateTime) {
        String dayOfWeek = "";
        switch (dateTime.getDayOfWeek()) {
            case DateTimeConstants.SUNDAY:
                dayOfWeek = "周日";
                break;
            case DateTimeConstants.MONDAY:
                dayOfWeek = "周一";
                break;
            case DateTimeConstants.TUESDAY:
                dayOfWeek = "周二";
                break;
            case DateTimeConstants.WEDNESDAY:
                dayOfWeek = "周三";
                break;
            case DateTimeConstants.THURSDAY:
                dayOfWeek = "周四";
                break;
            case DateTimeConstants.FRIDAY:
                dayOfWeek = "周五";
                break;
            case DateTimeConstants.SATURDAY:
                dayOfWeek = "周六";
            default:
                break;
        }
        return dayOfWeek;
    }

}
