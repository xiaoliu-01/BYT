package com.register.byt.statistics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author LLXX
 * @create 2021-08-25 11:09
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableFeignClients
@EnableDiscoveryClient
public class ServiceStatisticsApp {
    public static void main(String[] args) {
        SpringApplication.run(ServiceStatisticsApp.class,args);
    }
}
