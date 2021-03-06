package com.register.byt.hosp.controller.api;

import com.register.byt.commons.result.Result;
import com.register.byt.hosp.service.DepartmentService;
import com.register.byt.hosp.service.HospitalService;
import com.register.byt.hosp.service.ScheduleService;
import com.register.model.entity.hosp.Hospital;
import com.register.model.entity.hosp.Schedule;
import com.register.model.vo.hosp.DepartmentVo;
import com.register.model.vo.hosp.HospitalQueryVo;
import com.register.model.vo.hosp.ScheduleOrderVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;

/**
 * @author LLXX
 * @create 2021-08-10 15:03
 */
@Api(tags = "医院管理接口")
@RestController
@RequestMapping("/api/hosp/hospital")
@Slf4j
public class HospitalApiController {

    @Resource
    private HospitalService hospitalService;

    @Resource
    private DepartmentService departmentService;

    @Resource
    private ScheduleService scheduleService;

    @ApiOperation(value = "获取分页医院数据列表")
    @GetMapping("{page}/{limit}")
    public Result getHospitalPageData(@PathVariable Integer page,
                                      @PathVariable Integer limit,
                                      HospitalQueryVo hospitalQueryVo){
        Page<Hospital> pageModel = hospitalService.getPageHospital(page, limit, hospitalQueryVo);
        return Result.ok(pageModel);
    }

    @ApiOperation(value = "根据医院名称获取医院列表")
    @GetMapping("/findByHosName/{hosName}")
    public Result findHospitalListByHosName(
                                            @ApiParam(name = "hosName", value = "医院名称", required = true)
                                            @PathVariable String hosName){
        List<Hospital> hospitals =  hospitalService.getHospitalListByHosName(hosName);
        return Result.ok(hospitals);
    }

    @ApiOperation(value = "根据医院Code,获取科室列表")
    @GetMapping("department/{hosCode}")
    public Result getDepartmentListByHosCode(
                                             @ApiParam(name = "hosCode", value = "医院code", required = true)
                                             @PathVariable String hosCode){
        List<DepartmentVo> departmentVos = departmentService.findDeptTree(hosCode);
        return Result.ok(departmentVos);
    }

    @ApiOperation(value = "根据医院Code获取医院预约挂号详情")
    @GetMapping("/{hosCode}")
    public Result getHospDetailByHosCode(@ApiParam(name = "hosCode", value = "医院code", required = true)
                                         @PathVariable String hosCode){
        return  Result.ok(hospitalService.selectHospDetailByHosCode(hosCode));
    }

    @ApiOperation(value = "获取可预约排班数据")
    @GetMapping("auth/getBookingScheduleRule/{page}/{limit}/{hosCode}/{depCode}")
    public Result getBookingSchedule(
                                    @ApiParam(name = "page", value = "当前页码", required = true)
                                    @PathVariable Integer page,
                                    @ApiParam(name = "limit", value = "每页记录数", required = true)
                                    @PathVariable Integer limit,
                                    @ApiParam(name = "hosCode", value = "医院code", required = true)
                                    @PathVariable String hosCode,
                                    @ApiParam(name = "depCode", value = "科室code", required = true)
                                    @PathVariable String depCode) {
        HashMap<String, Object> map = scheduleService.getBookingScheduleRule(page, limit, hosCode, depCode);
        return Result.ok(map);
    }

    @ApiOperation(value = "获取排班数据")
    @GetMapping("auth/findScheduleList/{hosCode}/{depCode}/{workDate}")
    public Result findScheduleList(
            @ApiParam(name = "hosCode", value = "医院code", required = true)
            @PathVariable String hosCode,
            @ApiParam(name = "depCode", value = "科室code", required = true)
            @PathVariable String depCode,
            @ApiParam(name = "workDate", value = "排班日期", required = true)
            @PathVariable String workDate) {
        List<Schedule> schedules = scheduleService.getDetailSchedule(hosCode, depCode, workDate);
        return Result.ok(schedules);
    }

    @ApiOperation(value = "根据排班id获取排班数据")
    @GetMapping("getSchedule/{scheduleId}")
    public Result getSchedule(
            @ApiParam(name = "scheduleId", value = "排班id", required = true)
            @PathVariable String scheduleId) {
        Schedule schedule = scheduleService.getScheduleDetailById(scheduleId);
        return Result.ok(schedule);
    }

    @ApiOperation(value = "根据排班id获取预约下单数据")
    @GetMapping("inner/getScheduleOrderVo/{scheduleId}")
    public ScheduleOrderVo getScheduleOrderVo(
                                              @ApiParam(name = "scheduleId", value = "排班id", required = true)
                                              @PathVariable("scheduleId") String scheduleId) {
        return scheduleService.getScheduleOrderVo(scheduleId);
    }

}
