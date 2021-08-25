package com.register.byt.order.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.register.byt.order.mapper.RefundInfoMapper;
import com.register.byt.order.service.RefundInfoService;
import com.register.model.entity.order.PaymentInfo;
import com.register.model.entity.order.RefundInfo;
import com.register.model.enums.RefundStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * @author LLXX
 * @create 2021-08-24 14:04
 */
@Slf4j
@Service
public class RefundInfoServiceImpl extends ServiceImpl<RefundInfoMapper, RefundInfo> implements RefundInfoService {

    @Transactional(rollbackFor = Exception.class)
    @Override
    public RefundInfo saveRefundInfo(PaymentInfo paymentInfo) {
        QueryWrapper<RefundInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id", paymentInfo.getOrderId())
                .eq("payment_type", paymentInfo.getPaymentType());
        RefundInfo refundInfo = baseMapper.selectOne(wrapper);
        if (refundInfo != null) return refundInfo;
        // 保存交易记录
        refundInfo = new RefundInfo();
        refundInfo.setCreateTime(LocalDateTime.now());
        refundInfo.setOrderId(paymentInfo.getOrderId());
        refundInfo.setPaymentType(paymentInfo.getPaymentType());
        refundInfo.setOutTradeNo(paymentInfo.getOutTradeNo());
        refundInfo.setRefundStatus(RefundStatusEnum.UNREFUND.getStatus());
        refundInfo.setSubject(paymentInfo.getSubject());
        //paymentInfo.setSubject("test");
        refundInfo.setTotalAmount(paymentInfo.getTotalAmount());
        baseMapper.insert(refundInfo);
        return refundInfo;
    }
}
