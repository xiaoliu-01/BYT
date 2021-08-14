package com.register.byt.user.controller;

import com.register.byt.commons.result.Result;
import com.register.byt.user.service.UserInfoService;
import com.register.model.vo.user.LoginVo;
import com.register.model.vo.user.RegisterVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author LLXX
 * @create 2021-08-11 16:24
 */
@Api(tags = "用户管理")
@RestController
@RequestMapping("/api/user")
@Slf4j
public class UserInfoController {

    @Resource
    private UserInfoService userInfoService;

    @ApiOperation(value = "用户登录，手机号方式")
    @PostMapping("/login")
    public Result login(@RequestBody LoginVo loginVo){
        Map<String,Object> map = userInfoService.login(loginVo);
        return Result.ok(map);
    }

    @ApiOperation(value = "用户注册，手机号方式")
    @PostMapping("/register")
    public Result register(@RequestBody RegisterVo registerVo){
        boolean result = userInfoService.register(registerVo);
        return Result.ok(result);
    }
}
