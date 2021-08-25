package com.register.byt.msm.listener;

import com.rabbitmq.client.Channel;
import com.register.byt.msm.service.MsmService;
import com.register.byt.rabbit.constant.MqConst;
import com.register.model.vo.msm.MsmVo;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import sun.plugin2.message.Message;

import javax.annotation.Resource;

/**
 * @author LLXX
 * @create 2021-08-21 14:53
 */
@Component
public class MsmListener {
    @Resource
    private MsmService msmService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_MSM_ITEM, durable = "true"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_MSM),
            key = {MqConst.ROUTING_MSM_ITEM}
    ))
    public void send(MsmVo msmVo, Message message, Channel channel) {
        msmService.send(msmVo);
    }
}
