package com.register.byt.user.controller.api;

import com.register.byt.commons.result.Result;
import com.register.byt.commons.utils.JwtUtil;
import com.register.byt.user.service.PatientService;
import com.register.model.entity.user.Patient;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author LLXX
 * @create 2021-08-17 8:55
 */
@Api(tags = "就诊人管理")
@RestController
@RequestMapping("/api/user/patient")
public class PatientApiController {

    @Resource
    private PatientService patientService;

    @ApiOperation(value = "获取就诊人列表")
    @GetMapping("auth/findAll")
    public Result findAll(HttpServletRequest request){
        String token = request.getHeader("token");
        Long userId = JwtUtil.getUserId(token);
        List<Patient> list = patientService.findAllUserId(userId);
        return Result.ok(list);
    }

    @ApiOperation(value = "添加就诊人")
    @PostMapping("auth/save")
    public Result addPatient(HttpServletRequest request,
                             @RequestBody Patient patient){
        Long userId = JwtUtil.getUserId(request.getHeader("token"));
        patient.setUserId(userId);
        patientService.save(patient);
        return Result.ok();
    }

    @ApiOperation(value = "根据就诊人ID,查询就诊人")
    @GetMapping("auth/getPatient/{id}")
    public Result findPatientById(@PathVariable long id) {
        Patient patient = patientService.getPatientById(id);
        return Result.ok(patient);
    }

    @ApiOperation(value = "更新就诊人")
    @PostMapping("auth/exitPatient")
    public Result updatePatient(@RequestBody Patient patient) {
        System.out.println("patient = " + patient);
        patientService.updateById(patient);
        return Result.ok();
    }

    @ApiOperation(value = "删除就诊人")
    @DeleteMapping("auth/remove/{id}")
    public Result delPatient(@PathVariable Long id) {
        patientService.removeById(id);
        return Result.ok();
    }

    @ApiOperation(value = "根据就诊人ID,查询就诊人")
    @GetMapping("inner/getPatient/{id}")
    public Patient getPatientById(@PathVariable long id){
        Patient patient = patientService.getPatientById(id);
        return patient;
    }
}


