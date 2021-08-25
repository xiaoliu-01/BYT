package com.register.byt.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author LLXX
 * @create 2021-08-20 10:49
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class ServiceOrderApp {
    public static void main(String[] args) {
        SpringApplication.run(ServiceOrderApp.class,args);
    }
}


