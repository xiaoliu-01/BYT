package com.register.byt.hosp.service;

import com.register.model.entity.hosp.Schedule;
import com.register.model.vo.hosp.ScheduleOrderVo;
import com.register.model.vo.hosp.ScheduleQueryVo;
import org.springframework.data.domain.Page;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author LLXX
 * @create 2021-08-06 9:08
 */
public interface ScheduleService  {
    /**
     *  添加排班信息
     * @param paramMap
     */
    void saveSchedule(Map<String, Object> paramMap);

    /**
     *  查询排班信息
     * @param page 当前页
     * @param pageSize 页面大小
     * @param queryVo 查询参数封装类
     * @return
     */
    Page<Schedule> findSchedulePage(Integer page, Integer pageSize, ScheduleQueryVo queryVo);

    /**
     * 删除排班信息
     * @param hosCode 医院Code
     * @param hosScheduleId 排班ID
     */
    void removeScheduleByCode(String hosCode, String hosScheduleId);

    /**
     *  根据医院编号和科室编号,查询排班规则数据
     * @param page 当前页
     * @param limit 页面大小
     * @param hosCode 医院编号
     * @param depCode 科室编号
     * @return
     */
    Map<String, Object> getRuleSchedule(long page, long limit, String hosCode, String depCode);

    /**
     * 根据医院编号 、科室编号和工作日期，查询排班详细信息
     * @param hosCode 医院编号
     * @param depCode 科室编号
     * @param workDate 工作日期
     * @return
     */
    List<Schedule> getDetailSchedule(String hosCode, String depCode, String workDate);

    /**
     * 根据医院编号 、科室编号，查询可预约的排班信息带分页
     * @param page
     * @param limit
     * @param hosCode
     * @param depCode
     * @return
     */
    HashMap<String, Object> getBookingScheduleRule(Integer page, Integer limit, String hosCode, String depCode);

    /**
     * 获取排班详细
     * @param scheduleId 排班ID
     * @return
     */
    Schedule getScheduleDetailById(String scheduleId);

    /**
     * 根据排班id获取预约下单数据
     * @param scheduleId 排班ID
     */
    ScheduleOrderVo getScheduleOrderVo(String scheduleId);

    /**
     * 更新排班详细
     * @param schedule
     */
    boolean update(Schedule schedule);
}
