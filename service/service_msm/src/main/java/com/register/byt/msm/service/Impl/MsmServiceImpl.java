package com.register.byt.msm.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.register.byt.commons.result.ResultCodeEnum;
import com.register.byt.exception.BytException;
import com.register.byt.msm.service.MsmService;
import com.register.byt.msm.utils.SmsProperties;
import com.register.model.vo.msm.MsmVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author LLXX
 * @create 2021-08-13 9:34
 */
@Service
@Slf4j
public class MsmServiceImpl implements MsmService {

    @Override
    public boolean send(String phone, String fourBitCode) {
        // 判断手机号是否为空
        if(StringUtils.isEmpty(phone)) return false;

        //创建远程连接客户端对象
        DefaultProfile profile = DefaultProfile.getProfile(
                SmsProperties.REGION_Id,
                SmsProperties.KEY_ID,
                SmsProperties.KEY_SECRET);

        //创建远程连接的请求参数
        IAcsClient client = new DefaultAcsClient(profile);

        CommonRequest request = new CommonRequest();
        request.setMethod(MethodType.POST);
        request.setDomain("dysmsapi.aliyuncs.com");
        request.setVersion("2017-05-25");
        request.setAction("SendSms");
        request.putQueryParameter("RegionId", SmsProperties.REGION_Id);
        request.putQueryParameter("PhoneNumbers", phone);
//        log.info("手机号："+phone);
        request.putQueryParameter("SignName", SmsProperties.SIGN_NAME);
//        log.info("签名："+SmsProperties.SIGN_NAME);
        request.putQueryParameter("TemplateCode", SmsProperties.TEMPLATE_CODE);
//        log.info("模板代码："+templateCode);

        //验证码  使用json格式   {"code":"123456"}
        Map<String,Object> param = new HashMap();
        param.put("code",fourBitCode);

        request.putQueryParameter("TemplateParam", JSONObject.toJSONString(param));

        try {
            //使用客户端对象携带请求对象发送请求并得到响应结果
            CommonResponse response = client.getCommonResponse(request);
            String data = response.getData();
            log.info("响应结果:"+data);
            // 发送成功
            return response.getHttpResponse().isSuccess();
        } catch (ServerException e) {
            log.error("阿里云短信发送SDK调用失败：");
            log.error("ErrorCode=" + e.getErrCode());
            log.error("ErrorMessage=" + e.getErrMsg());
            throw new BytException(ResultCodeEnum.ALIYUN_SMS_ERROR.getMessage());
        } catch (ClientException e) {
            log.error("阿里云短信发送SDK调用失败：");
            log.error("ErrorCode=" + e.getErrCode());
            log.error("ErrorMessage=" + e.getErrMsg());
            throw new BytException(ResultCodeEnum.ALIYUN_SMS_ERROR.getMessage());
        }
    }

    // 生成订单，发送验证码
    @Override
    public boolean send(MsmVo msmVo) {
        Map<String, Object> map = msmVo.getParam();
        String phone = msmVo.getPhone();
        String code = (String) map.get("code");
        if(!StringUtils.isEmpty(phone)){
            return this.send(phone,code);
        }
        return false;
    }
}
