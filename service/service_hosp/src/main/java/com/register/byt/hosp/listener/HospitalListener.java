package com.register.byt.hosp.listener;

import com.rabbitmq.client.Channel;
import com.register.byt.hosp.service.ScheduleService;
import com.register.byt.rabbit.constant.MqConst;
import com.register.byt.rabbit.service.RabbitService;
import com.register.model.entity.hosp.Schedule;
import com.register.model.vo.order.OrderMqVo;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import sun.plugin2.message.Message;

import javax.annotation.Resource;

/**
 * @author LLXX
 * @create 2021-08-21 14:57
 */
@Component
public class HospitalListener {
    @Resource
    private ScheduleService scheduleService;

    @Resource
    private RabbitService rabbitService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_ORDER),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_ORDER),
            key = {MqConst.ROUTING_ORDER}
    ))
    public boolean receiver(OrderMqVo orderMqVo , Message message, Channel channel){
        // 下单成功更新预约数
        if (orderMqVo.getAvailableNumber() != null) {
            Schedule schedule = scheduleService.getScheduleDetailById(orderMqVo.getScheduleId());
            schedule.setAvailableNumber(orderMqVo.getAvailableNumber());
            schedule.setReservedNumber(orderMqVo.getReservedNumber());
            scheduleService.update(schedule);
        } else {
            //取消预约更新预约数
            Schedule schedule = scheduleService.getScheduleDetailById(orderMqVo.getScheduleId());
            int availableNumber = schedule.getAvailableNumber().intValue() + 1;
            schedule.setAvailableNumber(availableNumber);
            scheduleService.update(schedule);
        }
        // 发送短信
        if(orderMqVo.getMsmVo() != null){
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_MSM,MqConst.ROUTING_MSM_ITEM,orderMqVo.getMsmVo());
            return true;
        }
        return false;
    }
}
