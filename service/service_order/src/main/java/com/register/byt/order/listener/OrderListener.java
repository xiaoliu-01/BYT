package com.register.byt.order.listener;

import com.register.byt.order.service.OrderInfoService;
import com.register.byt.rabbit.constant.MqConst;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author LLXX
 * @create 2021-08-25 9:06
 */
@Component
public class OrderListener {

    @Resource
    private OrderInfoService orderInfoService;

    @RabbitListener(bindings = {@QueueBinding(
            value = @Queue(value = MqConst.QUEUE_TASK_8),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_TASK),
            key = {MqConst.ROUTING_TASK_8}
    )})
    public void patientTips (){
        orderInfoService.patientTips();
    }
}
