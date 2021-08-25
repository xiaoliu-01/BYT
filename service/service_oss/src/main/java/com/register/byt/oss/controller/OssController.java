package com.register.byt.oss.controller;

import com.register.byt.commons.result.Result;
import com.register.byt.oss.service.OssService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

/**
 * @author LLXX
 * @create 2021-08-15 15:36
 */
@Api(tags = "Oss存储管理")
@RestController
@RequestMapping("/api/oss/")
public class OssController {

    @Resource
    private OssService ossService;

    @ApiOperation("文件上传")
    @PostMapping("/upload")
    public Result fileUpload(MultipartFile file){
        // 返回文件url
        String url = ossService.upload(file);
        return Result.ok(url);
    }
}
