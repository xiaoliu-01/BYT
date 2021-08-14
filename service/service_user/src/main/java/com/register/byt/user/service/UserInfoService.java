package com.register.byt.user.service;

import com.register.model.vo.user.LoginVo;
import com.register.model.vo.user.RegisterVo;

import java.util.Map;

/**
 * @author LLXX
 * @create 2021-08-11 16:19
 */
public interface UserInfoService {

    /**
     * 用户登录
     * @param loginVo
     * @return
     */
    Map<String, Object> login(LoginVo loginVo);

    /**
     * 用户注册
     * @param registerVo
     * @return
     */
    boolean register(RegisterVo registerVo);
}
