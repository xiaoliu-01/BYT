package com.register.byt.task;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author LLXX
 * @create 2021-08-25 8:46
 */
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@EnableDiscoveryClient
public class ServiceTaskApp {
    public static void main(String[] args) {
        SpringApplication.run(ServiceTaskApp.class,args);
    }
}
