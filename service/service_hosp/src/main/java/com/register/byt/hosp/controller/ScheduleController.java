package com.register.byt.hosp.controller;

import com.register.byt.commons.result.Result;
import com.register.byt.hosp.service.ScheduleService;
import com.register.model.entity.hosp.Schedule;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author LLXX
 * @create 2021-08-08 16:49
 */
@Api(tags = "排班管理")
@RestController
@RequestMapping("/admin/hosp/schedule")
//@CrossOrigin
public class ScheduleController {

    @Resource
    private ScheduleService scheduleService;

    @ApiOperation(value ="根据医院编号和科室编号,查询排班规则数据")
    @GetMapping("getScheduleRule/{page}/{limit}/{hosCode}/{depCode}")
    public Result getScheduleRule(@PathVariable long page,
                                  @PathVariable long limit,
                                  @PathVariable String hosCode,
                                  @PathVariable String depCode) {
        Map<String,Object> map = scheduleService.getRuleSchedule(page,limit,hosCode,depCode);
        return Result.ok(map);
    }

    @ApiOperation(value = "根据医院编号 、科室编号和工作日期，查询排班详细信息")
    @GetMapping("getScheduleDetail/{hosCode}/{depCode}/{workDate}")
    public Result getScheduleDetail( @PathVariable String hosCode,
                                     @PathVariable String depCode,
                                     @PathVariable String workDate) {
        List<Schedule> list = scheduleService.getDetailSchedule(hosCode,depCode,workDate);
        return Result.ok(list);
    }

}
