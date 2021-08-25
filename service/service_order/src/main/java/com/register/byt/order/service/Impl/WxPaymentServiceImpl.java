package com.register.byt.order.service.Impl;

import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import com.register.byt.order.service.OrderInfoService;
import com.register.byt.order.service.PaymentService;
import com.register.byt.order.service.RefundInfoService;
import com.register.byt.order.service.WxPaymentService;
import com.register.byt.order.utils.HttpClient;
import com.register.byt.order.utils.WxPaymentProperties;
import com.register.model.entity.order.OrderInfo;
import com.register.model.entity.order.PaymentInfo;
import com.register.model.entity.order.RefundInfo;
import com.register.model.enums.PaymentTypeEnum;
import com.register.model.enums.RefundStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author LLXX
 * @create 2021-08-23 9:29
 */
@Service
@Slf4j
public class WxPaymentServiceImpl implements WxPaymentService {

    @Resource
    private OrderInfoService orderInfoService;

    @Resource
    private PaymentService paymentService;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private RefundInfoService refundInfoService;

    @Override
    public Map<String, Object> createNative(Long orderId) {
        Map payMap  = (Map)redisTemplate.opsForValue().get(orderId.toString());
        if(payMap != null){
            log.info("从Redis中取值...");
            return payMap;
        }
        // 根据id获取订单信息
        OrderInfo orderInfo = orderInfoService.getById(orderId);
        // 保存交易记录
        boolean saveResult = paymentService.savePaymentInfo(orderInfo, PaymentTypeEnum.WEIXIN.getStatus());
        log.info("保存" + saveResult );
        // 1、设置参数
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("appid", WxPaymentProperties.APP_ID);
        paramMap.put("mch_id", WxPaymentProperties.PARTNER);
        paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
        String body = new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd") + " 就诊 "+ orderInfo.getDepname();
        paramMap.put("body", body);
        paramMap.put("out_trade_no", orderInfo.getOutTradeNo());
        //paramMap.put("total_fee", order.getAmount().multiply(new BigDecimal("100")).longValue()+"");
        paramMap.put("total_fee", "1"); // 测试方便，设置为一分钱
        paramMap.put("spbill_create_ip", "127.0.0.1");
        paramMap.put("notify_url", "http://guli.shop/api/order/weixinPay/weixinNotify");
        paramMap.put("trade_type", "NATIVE");

        try {
            // 2、HTTPClient来根据URL访问第三方接口并且传递参数
            //String url = "https://api.mch.weixin.qq.com/pay/unifiedorder";
            HttpClient client = new HttpClient(WXPayConstants.UNIFIEDORDER_URL);
            client.setHttps(true); // 开启发送https请求
            client.setXmlParam(WXPayUtil.generateSignedXml(paramMap,WxPaymentProperties.PARTNER_KEY));
            client.post();
            // 3、返回第三方的数据
            String strXml = client.getContent(); // 返回xml数据
            Map<String, String> xmlToMap = WXPayUtil.xmlToMap(strXml);
            // 4、封装返回结果集
            Map<String,Object> map = new HashMap<>();
            map.put("orderId", orderId);
            map.put("totalFee", orderInfo.getAmount());
            map.put("resultCode", xmlToMap.get("result_code"));
            map.put("codeUrl", xmlToMap.get("code_url")); // 二维码地址
            if(!StringUtils.isEmpty(map.get("codeUrl"))){
                redisTemplate.opsForValue().set(orderId.toString(),map,2, TimeUnit.HOURS); // 存两小时
                log.info("存值到Redis中...");
            }
            log.info("返回数据：" + map.toString());
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Map<String, String> queryPayStatus(Long orderId, Integer payTypeStatus) {
        try {
            // 1.查询订单详细
            OrderInfo orderInfo = orderInfoService.getById(orderId);

            // 2、封装参数,并请求
            Map<String, String> paramsMap = new HashMap<>();
            paramsMap.put("appid",WxPaymentProperties.APP_ID);
            paramsMap.put("mch_id",WxPaymentProperties.PARTNER);
            paramsMap.put("out_trade_no",orderInfo.getOutTradeNo());
            paramsMap.put("nonce_str",WXPayUtil.generateNonceStr());
            String url = "https://api.mch.weixin.qq.com/pay/orderquery";
            HttpClient client = new HttpClient(url);
            client.setXmlParam(WXPayUtil.generateSignedXml(paramsMap,WxPaymentProperties.PARTNER_KEY));
            client.setHttps(true);
            client.post();
            // 3、返回第三方的数据，转成Map
            String xml = client.getContent();
            Map<String, String> map = WXPayUtil.xmlToMap(xml);
            // 4、返回数据
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Boolean refund(Long orderId) {
        try {
            // 获取支付详细，并保存退款信息
            PaymentInfo paymentInfo = paymentService.getPaymentInfo(orderId, PaymentTypeEnum.WEIXIN.getStatus());
            RefundInfo refundInfo = refundInfoService.saveRefundInfo(paymentInfo);
            // 封装参数,并发送请求
            Map<String,String> paramMap = new HashMap<>();
            paramMap.put("appid",WxPaymentProperties.APP_ID);       //公众账号ID
            paramMap.put("mch_id",WxPaymentProperties.PARTNER);   //商户编号
            paramMap.put("nonce_str",WXPayUtil.generateNonceStr());
            paramMap.put("transaction_id",paymentInfo.getTradeNo()); //微信订单号
            paramMap.put("out_trade_no",paymentInfo.getOutTradeNo()); //商户订单编号
            paramMap.put("out_refund_no","tk"+paymentInfo.getOutTradeNo()); //商户退款单号
//       paramMap.put("total_fee",paymentInfoQuery.getTotalAmount().multiply(new BigDecimal("100")).longValue()+"");
//       paramMap.put("refund_fee",paymentInfoQuery.getTotalAmount().multiply(new BigDecimal("100")).longValue()+"");
            paramMap.put("total_fee","1");
            paramMap.put("refund_fee","1");
            String paramXml  = WXPayUtil.generateSignedXml(paramMap, WxPaymentProperties.PARTNER_KEY);
            //String url = "https://api.mch.weixin.qq.com/secapi/pay/refund";
            HttpClient client = new HttpClient(WXPayConstants.REFUND_URL);
            client.setHttps(true);
            client.setXmlParam(paramXml);
            client.setCertPassword(WxPaymentProperties.PARTNER);
            client.setCert(true);
            client.post();
            // 获取返回数据
            String xml = client.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xml);
            if (null != resultMap && WXPayConstants.SUCCESS.equalsIgnoreCase(resultMap.get("result_code"))){
                // 退款成功，更新退款状态
                refundInfo.setCallbackTime(new Date());
                refundInfo.setTradeNo(resultMap.get("refund_id"));
                refundInfo.setRefundStatus(RefundStatusEnum.REFUND.getStatus());
                refundInfoService.updateById(refundInfo);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }
}
