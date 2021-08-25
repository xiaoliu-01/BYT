package com.register.byt.user.utils;

import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author LLXX
 * @create 2021-08-14 10:19
 */
@Data
@Component
@ConfigurationProperties(prefix = "wx.open")
public class WxProperties implements InitializingBean {

    public String app_id;
    public String app_secret;
    public String redirect_url;

    public static String WX_OPEN_APP_ID;
    public static String WX_OPEN_APP_SECRET;
    public static String WX_OPEN_REDIRECT_URL;

    @Override
    public void afterPropertiesSet() throws Exception {
        WX_OPEN_APP_ID = app_id;
        WX_OPEN_APP_SECRET = app_secret;
        WX_OPEN_REDIRECT_URL = redirect_url;
    }
}
