package com.register.byt.order.controller.api;

import com.register.byt.commons.result.Result;
import com.register.byt.order.service.PaymentService;
import com.register.byt.order.service.WxPaymentService;
import com.register.model.enums.PaymentTypeEnum;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author LLXX
 * @create 2021-08-23 9:26
 */
@Api(tags = "微信支付")
@RestController
@RequestMapping("/api/order/weixin")
public class WxPaymentController {

    @Resource
    private WxPaymentService wxPaymentService;

    @Resource
    private PaymentService paymentService;

    @ApiOperation(value = "下单 生成二维码")
    @GetMapping("/createNative/{orderId}")
    public Result createNative(
            @ApiParam(name = "orderId", value = "订单id", required = true)
            @PathVariable("orderId") Long orderId) {
        return Result.ok(wxPaymentService.createNative(orderId));
    }

    @ApiOperation(value = "查询支付状态")
    @GetMapping("queryPayStatus/{orderId}")
    public Result queryPayStatus(@PathVariable Long orderId){
        Map<String , String> map = wxPaymentService.queryPayStatus(orderId, PaymentTypeEnum.WEIXIN.getStatus());
        if("SUCCESS".equals(map.get("trade_state"))){ // 支付成功
            //更改订单状态，处理支付结果
            String outTradeNo = map.get("out_trade_no");
            paymentService.paySuccess(outTradeNo,map,PaymentTypeEnum.WEIXIN.getStatus());
            return Result.ok().message("支付成功");
        }
        return Result.ok().message("支付中");
    }
}
