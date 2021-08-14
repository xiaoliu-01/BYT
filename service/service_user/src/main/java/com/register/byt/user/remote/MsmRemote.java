package com.register.byt.user.remote;

import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author LLXX
 * @create 2021-08-13 11:19
 */
@FeignClient(value = "service-msm")
public interface MsmRemote {


}
