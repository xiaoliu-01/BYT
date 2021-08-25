package com.register.byt.order.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.register.byt.commons.result.ResultCodeEnum;
import com.register.byt.exception.BytException;
import com.register.byt.order.mapper.PaymentMapper;
import com.register.byt.order.remote.HospitalRemoteClient;
import com.register.byt.order.service.OrderInfoService;
import com.register.byt.order.service.PaymentService;
import com.register.model.entity.order.OrderInfo;
import com.register.model.entity.order.PaymentInfo;
import com.register.model.enums.OrderStatusEnum;
import com.register.model.enums.PaymentStatusEnum;
import com.register.model.helper.HttpRequestHelper;
import com.register.model.vo.order.SignInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author LLXX
 * @create 2021-08-23 9:34
 */
@Service
@Slf4j
public class PaymentServiceImpl extends ServiceImpl<PaymentMapper, PaymentInfo> implements PaymentService {

    @Resource
    private OrderInfoService orderInfoService;

    @Resource
    private HospitalRemoteClient hospitalRemoteClient;

    @Override
    public boolean savePaymentInfo(OrderInfo orderInfo, Integer paymentType) {
        Long orderId = orderInfo.getId();
        QueryWrapper<PaymentInfo> wrapper = new QueryWrapper<>();
        wrapper.eq(!StringUtils.isEmpty(orderId),"order_id",orderId)
                .eq("payment_type",paymentType);
        Integer count = baseMapper.selectCount(wrapper);
        if(count > 0) {return false;}
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOrderId(orderInfo.getId());
        paymentInfo.setPaymentType(paymentType);
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setPaymentStatus(PaymentStatusEnum.UNPAID.getStatus());
        String subject = new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd")+"|"+orderInfo.getHosname()+"|"+orderInfo.getDepname()+"|"+orderInfo.getTitle();
        paymentInfo.setSubject(subject);
        paymentInfo.setTotalAmount(orderInfo.getAmount());
        baseMapper.insert(paymentInfo);
        return true;
    }

    @Override
    public void paySuccess(String outTradeNo, Map<String, String> map, Integer status) {
        // 1、获取支付详细
        QueryWrapper<PaymentInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("out_trade_no",outTradeNo).eq("payment_type",status);
        PaymentInfo paymentInfo = this.getOne(wrapper);
        // 2、更新支付状态
        paymentInfo.setPaymentStatus(PaymentStatusEnum.PAID.getStatus());
        paymentInfo.setTradeNo(map.get("transaction_id"));
        paymentInfo.setCallbackTime(new Date());
        paymentInfo.setCallbackContent(map.toString());
        paymentInfo.getParam().put("orderStatusString",PaymentStatusEnum.PAID.getName());
        this.updateById(paymentInfo);
        // 3、更新订单信息
        OrderInfo orderInfo = orderInfoService.getById(paymentInfo.getOrderId());
        orderInfo.setOrderStatus(OrderStatusEnum.PAID.getStatus());
        orderInfoService.updateById(orderInfo);
        // 4、调用医院服务，更新医院订单状态
        SignInfoVo signInfoVo = hospitalRemoteClient.getSignInfoVo(orderInfo.getHoscode());
        if(signInfoVo == null){
            throw new BytException(ResultCodeEnum.DATA_ERROR);
        }
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("hoscode",orderInfo.getHoscode());
        paramMap.put("hosRecordId",orderInfo.getHosRecordId());
        paramMap.put("timestamp", HttpRequestHelper.getTimestamp());
        String sign = HttpRequestHelper.getSign(paramMap, signInfoVo.getSignKey());
        paramMap.put("sign",sign);
        JSONObject sendRequest =
                HttpRequestHelper.sendRequest(paramMap, signInfoVo.getApiUrl() + "/order/updatePayStatus");
        if(sendRequest.getInteger("code") != 200) {
            throw new BytException(sendRequest.getString("message"), ResultCodeEnum.FAIL.getCode());
        }
    }

    @Override
    public PaymentInfo getPaymentInfo(Long orderId, Integer paymentType) {
        QueryWrapper<PaymentInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id",orderId).eq("payment_type",paymentType);
        PaymentInfo paymentInfo = baseMapper.selectOne(wrapper);
        return paymentInfo;
    }
}
