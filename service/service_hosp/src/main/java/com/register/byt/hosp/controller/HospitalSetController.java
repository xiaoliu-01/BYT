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
@RequestMapping("/admin/hosp/hospitalSet")
@CrossOrigin
public class HospitalSetController {
    @Resource
    private HospitalSetService hospitalSetService;

    @ApiOperation("获取医院设置信息列表")
    @GetMapping("listAll")
    public Result<List<HospitalSet>> getListAll() {
        List<HospitalSet> list = hospitalSetService.list();
        return Result.ok(list);
    }

    @ApiOperation("根据医院编号删除单个医院")
    @DeleteMapping("/delete/{Code}")
    public Result delHospitalSetById(@ApiParam("医院编号")
                                    @PathVariable("Code") String Code) {
        try {
            hospitalSetService.remove(new QueryWrapper<HospitalSet>().eq("hoscode",Code));
        } catch (Exception e) {
            return Result.fail().message("删除失败！！");
        }
        return Result.ok().message("删除成功！！");
    }

    @ApiOperation("分页查询医院列表")
    @PostMapping("/findPageHospitalSet/{current}/{limit}")
    public Result findPageHospitalSet(@ApiParam("起始页") @PathVariable Long current,
                                      @ApiParam("页面大小") @PathVariable Long limit,
                                      @ApiParam("查询条件") @RequestBody(required = false) HospitalQueryVo queryVo) {
        IPage<HospitalSet> iPage = new Page(current, limit);
        QueryWrapper<HospitalSet> wrapper = new QueryWrapper<>();
        // 封装查询条件
        wrapper.eq(!StringUtils.isEmpty(queryVo.getHoscode()), "hoscode", queryVo.getHoscode())
                .eq(queryVo.getStatus() != null, "status", queryVo.getStatus())
                .like(!StringUtils.isEmpty(queryVo.getHosname()), "hosname", queryVo.getHosname());

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
        return save ? Result.ok().message("保存成功！！") : Result.fail().message("保存失败！！");
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
        return ret ? Result.ok().message("更新成功！！") : Result.fail().message("更新失败！！");
    }

    @ApiOperation("批量删除医院信息")
    @PutMapping("/batchRemoveHospitalSet")
    public Result batchRemoveHospitalSet(@ApiParam("批量删除ID集")
                                         @RequestBody List<Long> ids){
        System.out.println("ids = " + ids);
        boolean ret = hospitalSetService.removeByIds(ids);
        return ret ? Result.ok().message("批量删除成功！！") : Result.fail().message("批量删除失败！！");
    }

    @ApiOperation("医院的解锁与锁定")
    @PutMapping("/lockHospitalSet/{id}/{status}")
    public Result lockHospitalSet(@ApiParam("解锁与锁定医院的ID") @PathVariable long id,
                                  @ApiParam("更新医院的锁定状态") @PathVariable Integer status){
        HospitalSet hospitalSet = hospitalSetService.getById(id);
        hospitalSet.setStatus(status);
        boolean update = hospitalSetService.updateById(hospitalSet);
        return update ? Result.ok().message("更新医院锁定状态成功！！") : Result.fail().message("更新医院锁定状态失败！！");
    }

    @ApiOperation("发送签名秘钥")
    @PutMapping("/sendSignKey/{id}")
    public Result sendSignKey(@ApiParam("要发送签名密钥的医院ID") @PathVariable long id){
        HospitalSet hospitalSet = hospitalSetService.getById(id);
        String hosCode = hospitalSet.getHoscode(); // 医院代码
        String signKey = hospitalSet.getSignKey(); // 签名密钥
        // TODO 发送短信
        return Result.ok();
    }
}
