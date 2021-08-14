package com.register.byt.msm.controller;

import com.register.byt.commons.RandomUtils;
import com.register.byt.commons.result.Result;
import com.register.byt.msm.service.MsmService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @author LLXX
 * @create 2021-08-13 9:31
 */
@Api(tags="短信服务")
@RestController
@RequestMapping("/api/msm")
public class MsmController {
    @Resource
    private MsmService msmService;

    @Resource
    private RedisTemplate<String,String> redisTemplate;

    @ApiOperation(value = "发送验证码")
    @GetMapping("/send/{phone}")
    public Result sendCode(@PathVariable String phone){
        // 从redis中获取验证码
        String code = redisTemplate.opsForValue().get("phone");
        if(code != null){
            return Result.ok();
        }else {
            //如果从redis获取不到，
            // 生成验证码，
            String fourBitCode = RandomUtils.getFourBitRandom();
            boolean result = msmService.send(phone,fourBitCode);
                if(result){
                    // 发送成功，将短信存入redis
                    redisTemplate.opsForValue().set("phone",fourBitCode,5, TimeUnit.MINUTES);
                    return Result.ok();
                }
        }
        return Result.fail().message("验证码，发送失败");
    }
}
