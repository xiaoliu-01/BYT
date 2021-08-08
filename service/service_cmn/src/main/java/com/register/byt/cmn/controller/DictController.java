package com.register.byt.cmn.controller;

import com.register.byt.cmn.service.DictService;
import com.register.byt.commons.result.Result;
import com.register.model.entity.cmn.Dict;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author LLXX
 * @create 2021-08-01 17:12
 */
@Api(tags = "数据字典接口")
@RestController
@RequestMapping("/admin/cmn/dict")
@CrossOrigin
public class DictController {
    @Resource
    private DictService dictService;

    @ApiOperation(value = "根据数据id查询子数据列表")
    @GetMapping("findChildData/{id}")
    public Result findChildDataById(@PathVariable Long id) {
        List<Dict> list = dictService.findChildDataById(id);
        return Result.ok(list);
    }

    @ApiOperation(value = "导出数据字典")
    @GetMapping(value = "/exportData")
    public Result exportData(HttpServletResponse response) {
        dictService.exportData(response);
        return Result.ok();
    }

    @ApiOperation(value = "导入数据字典")
    @PostMapping(value = "/importData")
    public Result importData(MultipartFile file) {
        dictService.importData(file);
        return Result.ok();
    }

    @ApiOperation(value = "获取数据字典名称")
    @GetMapping(value = "/getName/{parentDictCode}/{value}")
    public String getDictName(@ApiParam(name = "parentDictCode", value = "上级编码", required = true)
                              @PathVariable("parentDictCode") String parentDictCode,
                              @ApiParam(name = "value", value = "值", required = true)
                              @PathVariable("value") String value) {
        return dictService.getDictNameByParentDictCodeAndValue(parentDictCode, value);

    }

    @ApiOperation(value = "获取数据字典名称")
    @GetMapping(value = "/getName/{value}")
    public String getDictName(@ApiParam(name = "value", value = "值", required = true)
                                  @PathVariable("value") String value) {
        return dictService.getDictNameByParentDictCodeAndValue("", value);
    }

    @ApiOperation(value = "根据dictCode获取下级节点")
    @GetMapping(value = "/findByDictCode/{dictCode}")
    public Result findByDictCode(
            @ApiParam(name = "dictCode", value = "节点编码", required = true)
            @PathVariable String dictCode) {
        List<Dict> dictList = dictService.findByDictCode(dictCode);
        return Result.ok(dictList);
    }
}