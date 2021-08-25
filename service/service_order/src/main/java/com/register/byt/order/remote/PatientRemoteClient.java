package com.register.byt.order.remote;

import com.register.model.entity.user.Patient;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author LLXX
 * @create 2021-08-20 11:15
 * 就诊人远程调用接口
 */
@FeignClient(value = "service-user")
@Component
public interface PatientRemoteClient {

    @ApiOperation(value = "根据就诊人ID,查询就诊人")
    @GetMapping("/api/user/patient/inner/getPatient/{id}")
    Patient getPatientById(@PathVariable long id);

}
