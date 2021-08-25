package com.register.byt.user.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.register.byt.commons.result.Result;
import com.register.byt.user.service.UserInfoService;
import com.register.model.entity.user.UserInfo;
import com.register.model.vo.user.UserInfoQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author LLXX
 * @create 2021-08-17 15:25
 */
@Api(tags = "用户平台管理")
@RestController
@RequestMapping("/admin/user/")
public class UserInfoController {

    @Resource
    private UserInfoService userInfoService;

    @ApiOperation(value = "用户列表")
    @GetMapping("{page}/{limit}")
    public Result findUserInfoPage(@PathVariable long  page,
                                   @PathVariable long limit,
                                   UserInfoQueryVo queryVo){
        Page<UserInfo> infoPage = new Page<>(page, limit);
        IPage<UserInfo> selectPage = userInfoService.selectPage(infoPage,queryVo);
        return Result.ok(selectPage);
    }

    @ApiOperation(value = "用户锁定")
    @PutMapping("lock/{userId}/{status}")
    public Result userInfoLock(@PathVariable Long userId,
                               @PathVariable Integer status){

        userInfoService.lock(userId,status);
        return Result.ok().message(status.intValue() == 0 ? "锁定成功" : "解锁成功");
    }

    @ApiOperation(value = "用户详情")
    @GetMapping("show/{userId}")
    public Result userInfoDetails(@PathVariable Long userId){
        Map<String,Object> map = userInfoService.showUserInfo(userId);
        return Result.ok(map);
    }

    @ApiOperation(value = "用户审批")
    @GetMapping("approval/{userId}/{authStatus}")
    public Result approval(@PathVariable Long userId,@PathVariable Integer authStatus){
       userInfoService.approval(userId,authStatus);
        return Result.ok().message(authStatus.intValue() == 2 ? "审批通过" : "审批不通过");
    }
}

