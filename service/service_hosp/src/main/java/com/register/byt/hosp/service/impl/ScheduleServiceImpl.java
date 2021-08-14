package com.register.byt.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.register.byt.hosp.repository.ScheduleRepository;
import com.register.byt.hosp.service.DepartmentService;
import com.register.byt.hosp.service.HospitalService;
import com.register.byt.hosp.service.ScheduleService;
import com.register.model.entity.hosp.Schedule;
import com.register.model.vo.hosp.BookingScheduleRuleVo;
import com.register.model.vo.hosp.ScheduleQueryVo;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author LLXX
 * @create 2021-08-06 9:08
 */
@Slf4j
@Service
public class ScheduleServiceImpl implements ScheduleService {

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
