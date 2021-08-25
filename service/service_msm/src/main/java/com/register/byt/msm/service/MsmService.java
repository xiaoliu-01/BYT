package com.register.byt.msm.service;

import com.register.model.vo.msm.MsmVo;

/**
 * @author LLXX
 * @create 2021-08-13 9:33
 */
public interface MsmService {

    /**
     * 发送验证码
     * @param phone 手机号
     * @param fourBitCode 四位码
     * @return
     */
    boolean send(String phone, String fourBitCode);

    /**
     * 发送验证码,生成订单
     */
    boolean send(MsmVo msmVo);
}
