package com.register.byt.task.task;

import com.register.byt.rabbit.constant.MqConst;
import com.register.byt.rabbit.service.RabbitService;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author LLXX
 * @create 2021-08-25 8:49
 */
@Component
@EnableScheduling
public class ScheduledTask {

    @Resource
    private RabbitService rabbitService;

    /**
     * 每天八点执行
     */
    @Scheduled(cron = "0 0 8 * * ? ")
    public void task(){
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_TASK,
                                  MqConst.ROUTING_TASK_8,"");
    }
}

