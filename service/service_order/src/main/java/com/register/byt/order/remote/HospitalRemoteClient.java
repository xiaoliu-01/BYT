package com.register.byt.order.remote;

import com.register.model.vo.hosp.ScheduleOrderVo;
import com.register.model.vo.order.SignInfoVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author LLXX
 * @create 2021-08-21 9:09
 * 医院远程调用接口
 */
@FeignClient("service-hosp")
@Component
public interface HospitalRemoteClient {

    @ApiOperation(value = "根据排班id获取预约下单数据")
    @GetMapping("/api/hosp/hospital/inner/getScheduleOrderVo/{scheduleId}")
    ScheduleOrderVo getScheduleOrderVo(@PathVariable("scheduleId") String scheduleId);

    @ApiOperation(value = "获取医院签名信息")
    @GetMapping("/admin/hosp/hospitalSet/inner/getSignInfoVo/{hosCode}")
    SignInfoVo getSignInfoVo(@PathVariable("hosCode") String hosCode);
}
