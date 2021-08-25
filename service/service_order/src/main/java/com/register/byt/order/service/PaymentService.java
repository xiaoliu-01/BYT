package com.register.byt.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.register.model.entity.order.OrderInfo;
import com.register.model.entity.order.PaymentInfo;

import java.util.Map;

/**
 * @author LLXX
 * @create 2021-08-23 9:34
 */
public interface PaymentService extends IService<PaymentInfo> {

    /**
     * 保存交易记录
     * @param orderInfo 订单详细
     * @param status 支付类型状态码
     * @return
     */
    boolean savePaymentInfo(OrderInfo orderInfo, Integer paymentType);

    /**
     * 支付成功，更新支付状态
     * @param outTradeNo 对外业务编号
     * @param map 回调支付数据
     * @param status 支付类型状态码
     */
    void paySuccess(String outTradeNo, Map<String, String> map, Integer status);

    /**
     * 获取支付记录
     * @param orderId
     * @param paymentType
     * @return
     */
    PaymentInfo getPaymentInfo(Long orderId, Integer paymentType);

}
