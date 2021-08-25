package com.register.byt.statistics.remote;

import com.register.byt.commons.result.Result;
import com.register.model.vo.order.OrderCountQueryVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author LLXX
 * @create 2021-08-25 11:13
 */
@FeignClient(value = "service-order")
public interface OrderInfoRemote {

    @PostMapping("/admin/order/orderCount")
    Result orderInfoCount(@RequestBody OrderCountQueryVo countQueryVo);
}
