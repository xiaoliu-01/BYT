package com.register.byt.msm.config;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author LLXX
 * @create 2021-08-13 9:25
 */
@Configuration
@ComponentScan({"com.register.byt.*"})
@EnableDiscoveryClient
public class MsmConfig {

}
