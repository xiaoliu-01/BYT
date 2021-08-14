package com.register.byt.hosp.controller;

import com.register.byt.commons.result.Result;
import com.register.byt.hosp.service.HospitalService;
import com.register.model.entity.hosp.Hospital;
import com.register.model.vo.hosp.HospitalQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author LLXX
 * @create 2021-08-06 10:39
 */
@Api(tags = "医院管理接口")
@RestController
@RequestMapping("/admin/hosp/hospital")
//@CrossOrigin // 跨域
public class HospitalController {

    @Resource
    private HospitalService hospitalService;

    @ApiOperation(value = "获取分页列表")
    @GetMapping("/list/{page}/{limit}")
    public Result getPageHospital(@PathVariable Integer page,
                                  @PathVariable Integer limit,
                                  HospitalQueryVo hospitalQueryVo){
        Page<Hospital> hospitals = hospitalService.getPageHospital(page,limit,hospitalQueryVo);
        return Result.ok(hospitals);
    }

    @ApiOperation("更新医院状态")
    @GetMapping("/updateStatus/{id}/{status}")
    public Result updateStatus( @ApiParam(name = "id", value = "医院id", required = true)
                                @PathVariable("id") String id,
                                @ApiParam(name = "status", value = "状态（0：未上线 1：已上线）", required = true)
                                @PathVariable("status") Integer status){
        hospitalService.updateStatus(id,status);
        return Result.ok().message(status == 0 ? "下线成功" : "上线成功");
    }

    @ApiOperation("获取医院详细信息")
    @GetMapping("/getHospitalById/{id}")
    public Result getHospitalById(@ApiParam(name = "id",value = "医院ID",required = true)
                                  @PathVariable String id){
        Hospital hospital = hospitalService.getHospitalById(id);
        return Result.ok(hospital);
    }
}
