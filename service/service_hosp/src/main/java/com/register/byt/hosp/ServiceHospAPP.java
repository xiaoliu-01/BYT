package com.register.byt.hosp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author LLXX
 * @create 2021-07-28 14:11
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class ServiceHospAPP {
    public static void main(String[] args) {
        SpringApplication.run(ServiceHospAPP.class,args);
    }
}
