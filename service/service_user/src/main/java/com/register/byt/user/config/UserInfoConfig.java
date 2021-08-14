package com.register.byt.user.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author LLXX
 * @create 2021-08-11 16:22
 */
@Configuration
@ComponentScan(value = {"com.register.byt.*"})
@MapperScan({"com.register.byt.user.mapper"})
public class UserInfoConfig {

}
