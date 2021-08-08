package com.register.byt.cmn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author LLXX
 * @create 2021-08-01 17:02
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ServiceCmnAPP {
    public static void main(String[] args) {
        SpringApplication.run(ServiceCmnAPP.class,args);
    }
}
