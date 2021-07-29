package com.register.byt.hosp.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.register.byt.commons.MD5;
import com.register.byt.commons.result.Result;
import com.register.byt.hosp.service.HospitalSetService;
import com.register.model.entity.hosp.HospitalSet;
import com.register.model.vo.hosp.HospitalQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Random;

/**
 * <p>
 * 医院设置表 前端控制器
 * </p>
 *
 * @author LLXX
 * @since 2021-07-28
 */
@Api(tags = "医院设置管理")
@RestController
@RequestMapping("/admin/hospitalSet")
public class HospitalSetController {
    @Resource
    private HospitalSetService hospitalSetService;

    @ApiOperation("获取医院设置信息列表")
    @GetMapping("listAll")
    public Result<List<HospitalSet>> getListAll() {
        List<HospitalSet> list = hospitalSetService.list();
        return Result.ok(list);
    }

    @ApiOperation("获取医院设置信息列表")
    @DeleteMapping("/delete/{Code}")
    public Result delHospitalSetById(
            @ApiParam("医院编号")
            @PathVariable("Code")
                    String Code) {
        hospitalSetService.removeById(Code);
        return Result.ok();
    }

    @ApiOperation("分页查询医院列表")
    @PostMapping("/findPageHospitalSet/{current}/{limit}")
    public Result findPageHospitalSet(@ApiParam("起始页") @PathVariable Long current,
                                      @ApiParam("页面大小") @PathVariable Long limit,
                                      @ApiParam("查询条件") @RequestBody(required = false) HospitalQueryVo queryVo) {
        IPage<HospitalSet> iPage = new Page(current, limit);
        QueryWrapper<HospitalSet> wrapper = new QueryWrapper<>();
        // 封装查询条件
        wrapper.eq(!StringUtils.isEmpty(queryVo.getHoscode()), "code", queryVo.getHoscode())
                .like(!StringUtils.isEmpty(queryVo.getHosname()), "name", queryVo.getHosname());

        IPage<HospitalSet> page = hospitalSetService.page(iPage, wrapper);
        return Result.ok(page);
    }

    @ApiOperation("添加医院信息")
    @PostMapping("/saveHospitalSet")
    public Result saveHospitalSet(@ApiParam("医院信息")
                                  @RequestBody HospitalSet hospitalSet){
        hospitalSet.setSignKey(MD5.encrypt(System.currentTimeMillis() + new Random().nextInt(1000)+""));
        hospitalSet.setStatus(1);
        boolean save = hospitalSetService.save(hospitalSet);
        return save ? Result.ok() : Result.fail().message("保存失败！！");
    }

    @ApiOperation("根据医院ID,获取医院详细信息")
    @GetMapping("/getHospitalSetById/{id}")
    public Result getHospitalSetById(@ApiParam("医院ID")
                                     @PathVariable long id){
        HospitalSet hospitalSet = hospitalSetService.getById(id);
        return Result.ok(hospitalSet);
    }

    @ApiOperation("更新医院详细信息")
    @PutMapping("/updateHospitalSetById")
    public Result updateHospitalSetById(@ApiParam("医院详细")
                                        @RequestBody HospitalSet hospitalSet){
        boolean ret = hospitalSetService.updateById(hospitalSet);
        return ret ? Result.ok() : Result.fail().message("更新失败！！");
    }

    @ApiOperation("批量删除医院信息")
    @PutMapping("/batchRemoveHospitalSet")
    public Result batchRemoveHospitalSet(@ApiParam("批量删除ID集")
                                         @RequestBody List<Long> ids){
        System.out.println("ids = " + ids);
        boolean ret = hospitalSetService.removeByIds(ids);
        return ret ? Result.ok() : Result.fail().message("批量删除失败！！");
    }
}
