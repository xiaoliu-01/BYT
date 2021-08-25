package com.register.byt.order.controller.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.register.byt.commons.result.Result;
import com.register.byt.commons.utils.JwtUtil;
import com.register.byt.order.service.OrderInfoService;
import com.register.model.entity.order.OrderInfo;
import com.register.model.enums.OrderStatusEnum;
import com.register.model.vo.order.OrderQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author LLXX
 * @create 2021-08-20 11:06
 */
@Api(tags = "订单管理")
@RestController
@RequestMapping("/api/order/")
public class OrderApiController {

    @Resource
    private OrderInfoService orderInfoService;

    @ApiOperation(value = "生成订单")
    @PostMapping("auth/submitOrder/{scheduleId}/{patientId}")
    public Result submitOrder(@PathVariable String scheduleId,@PathVariable Long patientId){
        Long orderId = orderInfoService.saveOrder(scheduleId, patientId);
        return Result.ok(orderId);
    }

    @ApiOperation(value = "订单详细")
    @GetMapping("auth/getOrders/{orderId}")
    public Result getOrderDetails(@PathVariable String orderId){
        OrderInfo orderInfo = orderInfoService.getOrderById(orderId);
        return Result.ok(orderInfo);
    }

    @ApiOperation("查询订单列表(带分页)")
    @GetMapping("{page}/{limit}")
    public Result getOrderList(@PathVariable Integer page, @PathVariable Integer limit,
                               OrderQueryVo orderQueryVo, HttpServletRequest request){
        String token = request.getHeader("token");
        Long userId = JwtUtil.getUserId(token);
        orderQueryVo.setUserId(userId);
        Page<OrderInfo> infoPage = new Page(page, limit);
        IPage<OrderInfo> iPage = orderInfoService.selectPage(orderQueryVo,infoPage);
        return Result.ok(iPage);
    }

    @ApiOperation(value = "获取订单状态")
    @GetMapping("auth/getStatusList")
    public Result getStatusList() {
        return Result.ok(OrderStatusEnum.getStatusList());
    }

    @ApiOperation(value = "取消预约")
    @GetMapping("auth/cancelOrder/{orderId}")
    public Result cancelOrder(
                             @ApiParam(name = "orderId", value = "订单id", required = true)
                             @PathVariable("orderId") Long orderId) {
        return Result.ok(orderInfoService.cancelOrder(orderId));
    }

}
