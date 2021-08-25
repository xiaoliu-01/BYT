package com.register.byt.hosp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.register.model.entity.hosp.HospitalSet;
import com.register.model.vo.order.SignInfoVo;

/**
 * <p>
 * 医院设置表 服务类
 * </p>
 *
 * @author LLXX
 * @since 2021-07-28
 */
public interface HospitalSetService extends IService<HospitalSet> {

    String getSingKey(String hoscode);

    /**
     * 获取医院签名信息
     * @param hosCode 医院Code
     * @return
     */
    SignInfoVo getSignInfoVo(String hosCode);
}
