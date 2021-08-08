package com.register.byt.hosp.controller;

import com.register.byt.commons.result.Result;
import com.register.byt.hosp.service.DepartmentService;
import com.register.model.vo.hosp.DepartmentVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author LLXX
 * @create 2021-08-08 15:16
 */
@Api(tags = "科室管理")
@RestController
@RequestMapping("/admin/hosp/department")
@CrossOrigin
public class DepartmentController {
    @Resource
    private DepartmentService departmentService;

    @ApiOperation(value = "根据医院Code,查询医院所有科室列表")
    @GetMapping("getDeptList/{hosCode}")
    public Result getDeptList(@ApiParam(name = "hosCode",value = "医院Code",required = true)
                              @PathVariable String hosCode) {
        List<DepartmentVo> list = departmentService.findDeptTree(hosCode);
        return Result.ok(list);
    }
}
