package com.register.byt.user.controller.api;

import com.register.byt.commons.result.Result;
import com.register.byt.commons.utils.JwtUtil;
import com.register.byt.user.service.UserInfoService;
import com.register.model.entity.user.UserInfo;
import com.register.model.enums.AuthStatusEnum;
import com.register.model.vo.user.LoginVo;
import com.register.model.vo.user.RegisterVo;
import com.register.model.vo.user.UserAuthVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author LLXX
 * @create 2021-08-11 16:24
 */
@Api(tags = "用户管理")
@RestController
@RequestMapping("/api/user")
@Slf4j
public class  UserInfoApiController {

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

    @ApiOperation(value = "用户认证接口")
    @PostMapping("auth/userAuth")
    public Result userAuth(@RequestBody UserAuthVo userAuthVo, HttpServletRequest request){
        String token = request.getHeader("token");
        Long userId = JwtUtil.getUserId(token);
        userInfoService.userAuth(userId,userAuthVo);
        return Result.ok();
    }

    @ApiOperation(value = "获取用户信息")
    @GetMapping("auth/getUserInfo")
    public Result getUserInfoById(HttpServletRequest request){
        String token = request.getHeader("token");
        Long userId = JwtUtil.getUserId(token);
        UserInfo userInfo = userInfoService.getById(userId);
        String statusNameByStatus = AuthStatusEnum.getStatusNameByStatus(userInfo.getAuthStatus());
        userInfo.getParam().put("authStatusString",statusNameByStatus);
        return Result.ok(userInfo);
    }
}
