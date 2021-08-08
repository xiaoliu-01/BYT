package com.register.byt.hosp.remote;

import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author LLXX
 * @create 2021-08-06 14:59
 */
@FeignClient(value = "service-cmn")
public interface CmnRemoteClient {

    @ApiOperation(value = "远程调用CMN服务接口,获取数据字典名称")
    @GetMapping(value = "/admin/cmn/dict/getName/{parentDictCode}/{value}")
    String getDictName(@PathVariable("parentDictCode")String parentDictCode,
                       @PathVariable("value")String value);

    @ApiOperation(value = "远程调用CMN服务接口,(没有上级字典Code)获取数据字典名称")
    @GetMapping(value = "/admin/cmn/dict/getName/{value}")
    String getDictName(@PathVariable("value") String value);

}
