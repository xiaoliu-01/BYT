package com.register.byt.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author LLXX
 * @create 2021-08-11 15:58
 */
@EnableFeignClients
@SpringBootApplication
public class ServiceUserApp {
    public static void main(String[] args) {
        SpringApplication.run(ServiceUserApp.class,args);
    }
}
