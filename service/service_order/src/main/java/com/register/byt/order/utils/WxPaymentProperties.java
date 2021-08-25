package com.register.byt.order.utils;

import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author LLXX
 * @create 2021-08-23 9:19
 */
@Data
@ConfigurationProperties(prefix = "weixin.pay")
public class WxPaymentProperties implements InitializingBean {

    private String appid;
    private String partner;
    private String partnerkey;
    private String cert;

    public static String APP_ID;
    public static String PARTNER;
    public static String PARTNER_KEY;
    public static String CERT;

    @Override
    public void afterPropertiesSet() throws Exception {
        APP_ID = appid;
        PARTNER = partner;
        PARTNER_KEY = partnerkey;
        CERT = cert;
    }
}