package com.register.byt.task.config;

import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author LLXX
 * @create 2021-08-20 10:56
 */
@Configuration
@ComponentScan({"com.register.byt.*"})
@ConfigurationPropertiesScan({"com.register.byt"})
public class TaskConfig {

}
