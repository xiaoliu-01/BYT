package com.register.byt.order.service;

import java.util.Map;

/**
 * @author LLXX
 * @create 2021-08-23 9:29
 */
public interface WxPaymentService {
    /**
     * 生成付款二维码
     * @param orderId 订单ID
     * @return
     */
    Map<String,Object> createNative(Long orderId);

    /**
     * 查询支付状态
     * @param orderId 订单ID
     * @param payTypeStatus 字符类型状态码
     * @return
     */
    Map<String, String> queryPayStatus(Long orderId, Integer payTypeStatus);

    /***
     * 退款
     * @param orderId
     * @return
     */
    Boolean refund(Long orderId);

}
