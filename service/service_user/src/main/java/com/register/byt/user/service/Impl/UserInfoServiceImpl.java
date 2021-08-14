package com.register.byt.user.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.register.byt.commons.JwtUtil;
import com.register.byt.commons.result.ResultCodeEnum;
import com.register.byt.exception.BytException;
import com.register.byt.user.mapper.UserInfoMapper;
import com.register.byt.user.service.UserInfoService;
import com.register.model.entity.user.UserInfo;
import com.register.model.vo.user.LoginVo;
import com.register.model.vo.user.RegisterVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author LLXX
 * @create 2021-08-11 16:20
 */
@Service
@Slf4j
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {

    @Resource
    private RedisTemplate<String,String> redisTemplate;

    @Override
    public Map<String, Object> login(LoginVo loginVo) {
        QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
        String code = loginVo.getCode();
        String phone = loginVo.getPhone();
        // 当code与手机号为空时，直接抛出异常
        if (StringUtils.isEmpty(code) || StringUtils.isEmpty(phone)) {
            log.warn("参数不正确");
            throw new BytException(ResultCodeEnum.PARAM_ERROR.getMessage());
        }
        // 校验校验验证码
        String phoneCode = redisTemplate.opsForValue().get("phone");
        log.info("code中的验证码为"+code);
        log.info("redis中的验证码为"+phoneCode);
        if(!code.equals(phoneCode)) {
            throw new BytException(ResultCodeEnum.CODE_ERROR);
        }

        // 用户是否存在
        wrapper.eq("phone", phone);
        UserInfo userInfo = this.getOne(wrapper);
        if (userInfo == null) { // 手机号未注册
            log.warn("手机号未注册");
            RegisterVo registerVo = new RegisterVo();
            registerVo.setMobile(loginVo.getPhone());
            this.register(registerVo);
        }
        // 校验是否被禁用
        if(userInfo.getStatus() == 0){
            log.warn("该用户已被禁用");
            throw new BytException(ResultCodeEnum.LOGIN_DISABLED_ERROR.getMessage());
        }
        // 返回页面显示名称
        HashMap<String, Object> map = new HashMap<>();
        String name = userInfo.getName();
        if(!StringUtils.isEmpty(name)) map.put("name",name);
        // 设置token
        String token = JwtUtil.createToken(userInfo.getId(), userInfo.getName());
        map.put("token", token);
        return map;
    }

    @Override
    public boolean register(RegisterVo registerVo) {
        UserInfo userInfo = new UserInfo();
        userInfo.setPhone(registerVo.getMobile());
        userInfo.setNickName(registerVo.getMobile());
        userInfo.setName(registerVo.getMobile());
        userInfo.setStatus(1);
        return this.save(userInfo);
    }
}
