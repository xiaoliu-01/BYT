package com.register.byt.order.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author LLXX
 * @create 2021-08-20 10:56
 */
@Configuration
@ComponentScan({"com.register.byt.*"})
@MapperScan({"com.register.byt.order.mapper"})
@ConfigurationPropertiesScan({"com.register.byt"})
public class OrderConfig {

}
