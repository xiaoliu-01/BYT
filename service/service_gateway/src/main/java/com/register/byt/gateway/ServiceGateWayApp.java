package com.register.byt.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author LLXX
 * @create 2021-08-09 15:04
 */
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@ComponentScan({"com.register.byt.*"})
public class ServiceGateWayApp {
    public static void main(String[] args) {
        SpringApplication.run(ServiceGateWayApp.class,args);
    }
}
