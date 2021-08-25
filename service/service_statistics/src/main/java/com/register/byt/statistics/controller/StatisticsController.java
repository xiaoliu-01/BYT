package com.register.byt.statistics.controller;

import com.register.byt.commons.result.Result;
import com.register.byt.statistics.remote.OrderInfoRemote;
import com.register.model.vo.order.OrderCountQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author LLXX
 * @create 2021-08-25 11:15
 */
@Api(tags = "统计管理接口")
@RestController
@RequestMapping("/admin/statistics")
public class StatisticsController {

    @Resource
    private OrderInfoRemote orderInfoRemote;

    @ApiOperation(value = "获取订单统计数据")
    @GetMapping("getCountMap")
    public Result getCountMap(@ApiParam(name = "orderCountQueryVo", value = "查询对象", required = false)
                                         OrderCountQueryVo orderCountQueryVo) {
        return Result.ok(orderInfoRemote.orderInfoCount(orderCountQueryVo));
    }
}
