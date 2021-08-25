package com.register.byt.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.register.model.entity.order.PaymentInfo;
import com.register.model.entity.order.RefundInfo;

/**
 * @author LLXX
 * @create 2021-08-24 14:04
 */
public interface RefundInfoService extends IService<RefundInfo> {

    /**
     * 保存退款记录
     * @param paymentInfo
     */
    RefundInfo saveRefundInfo(PaymentInfo paymentInfo);

}
