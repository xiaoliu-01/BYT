package com.register.byt.oss.utlis;

import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author LLXX
 * @create 2021-08-15 15:09
 */
@Data
@Component
@ConfigurationProperties(prefix = "aliyun.oss")
public class OssProperties implements InitializingBean {

    public String endpoint;
    public String accessKeyId;
    public String secret;
    public String bucket;

    public static String ENDPOINT;
    public static String ACCESS_KEY_ID;
    public static String SECRET;
    public static String BUCKET;

    @Override
    public void afterPropertiesSet() throws Exception {
        ENDPOINT = endpoint;
        ACCESS_KEY_ID = accessKeyId;
        SECRET = secret;
        BUCKET = bucket;
    }
}
