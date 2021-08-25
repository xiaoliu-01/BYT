package com.register.byt.order.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.register.byt.commons.result.Result;
import com.register.byt.order.service.OrderInfoService;
import com.register.model.entity.order.OrderInfo;
import com.register.model.enums.OrderStatusEnum;
import com.register.model.vo.order.OrderCountQueryVo;
import com.register.model.vo.order.OrderQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author LLXX
 * @create 2021-08-22 9:30
 */
@Api(tags = "订单管理")
@RestController
@RequestMapping("/admin/order")
public class OrderController {

    @Resource
    private OrderInfoService orderInfoService;

    @ApiOperation("查询订单列表(带分页)")
    @GetMapping("getOrders/{page}/{limit}")
    public Result getOrderList(@PathVariable Integer page, @PathVariable Integer limit,
                               OrderQueryVo orderQueryVo, HttpServletRequest request){
        Page<OrderInfo> infoPage = new Page(page, limit);
        IPage<OrderInfo> iPage = orderInfoService.selectPage(orderQueryVo,infoPage);
        return Result.ok(iPage);
    }

    @ApiOperation("获取状态")
    @GetMapping("getStatusList")
    public Result getStatusList(){
        return Result.ok(OrderStatusEnum.getStatusList());
    }

    @ApiOperation("获取订单详细")
    @GetMapping("getOrderDetail/{id}")
    public Result getOrderById(@PathVariable Long id ){
        Map<String,Object> orderInfo = orderInfoService.show(id);
        return Result.ok(orderInfo);
    }

    @ApiOperation(value = "统计订单")
    @PostMapping("orderCount")
    public Result orderInfoCount(OrderCountQueryVo countQueryVo){
        Map<String, Object> countMap = orderInfoService.getCountMap(countQueryVo);
        return Result.ok(countMap);
    }
}
