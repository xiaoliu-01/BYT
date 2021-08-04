package com.register.byt.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author LLXX
 * @create 2021-08-02 10:27
 */
@SpringBootApplication(exclude= {DataSourceAutoConfiguration.class})
@EnableDiscoveryClient
public class ServiceGateWayApp {
    public static void main(String[] args) {
        SpringApplication.run(ServiceGateWayApp.class,args);
    }
}
