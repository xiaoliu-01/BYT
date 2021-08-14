package com.register.byt.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * @author LLXX
 * @create 2021-08-09 15:04
 */
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class ServiceGateWayApp {
    public static void main(String[] args) {
        SpringApplication.run(ServiceGateWayApp.class,args);
    }
}
