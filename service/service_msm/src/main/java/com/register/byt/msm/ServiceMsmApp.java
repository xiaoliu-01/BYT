package com.register.byt.msm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * @author LLXX
 * @create 2021-08-13 9:25
 */
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class ServiceMsmApp {
    public static void main(String[] args) {
        SpringApplication.run(ServiceMsmApp.class,args);
    }
}
