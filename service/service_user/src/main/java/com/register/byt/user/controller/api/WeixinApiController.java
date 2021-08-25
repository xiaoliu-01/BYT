package com.register.byt.user.controller.api;

import com.alibaba.fastjson.JSONObject;
import com.register.byt.commons.result.Result;
import com.register.byt.commons.result.ResultCodeEnum;
import com.register.byt.commons.utils.HttpClientUtils;
import com.register.byt.commons.utils.JwtUtil;
import com.register.byt.exception.BytException;
import com.register.byt.user.service.UserInfoService;
import com.register.byt.user.utils.WxProperties;
import com.register.model.entity.user.UserInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * @author LLXX
 * @create 2021-08-14 10:26
 */
@Api(tags = "微信登录管理")
@Controller
@RequestMapping("/api/ucenter/wx")
@Slf4j
public class WeixinApiController {

    @Resource
    private UserInfoService userInfoService;

    // 1、生成微信扫描二维码
    @ApiOperation(value = "获取微信登录参数")
    @ResponseBody
    @GetMapping("getLoginParam")
    public Result genQrConnect(HttpSession session) throws UnsupportedEncodingException {
        String redirectUri = URLEncoder.encode(WxProperties.WX_OPEN_REDIRECT_URL, "UTF-8");
        Map<String, Object> map = new HashMap<>();
        map.put("appid", WxProperties.WX_OPEN_APP_ID);
        map.put("redirectUri", redirectUri);
        map.put("scope", "snsapi_login");
        map.put("state", System.currentTimeMillis() + ""); //System.currentTimeMillis()+""
        return Result.ok(map);
    }

    @ApiOperation("微信回调")
    @GetMapping("/callback")
    public String callback(String code, String state) {
        log.info("code:" + code);
        log.info("state:" + state);
        if (StringUtils.isEmpty(state) || StringUtils.isEmpty(code)) {
            log.error("非法回调请求");
            throw new BytException(ResultCodeEnum.ILLEGAL_CALLBACK_REQUEST_ERROR);
        }
        // 使用code和appid以及appscrect换取access_token
        // https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code
        StringBuffer baseAccessTokenUrl = new StringBuffer();
        baseAccessTokenUrl.append("https://api.weixin.qq.com/sns/oauth2/access_token")
                .append("?appid=%s")
                .append("&secret=%s")
                .append("&code=%s")
                .append("&grant_type=authorization_code");

        // 设置参数
        String accessTokenUrl = String.format(baseAccessTokenUrl.toString(),
                WxProperties.WX_OPEN_APP_ID,
                WxProperties.WX_OPEN_APP_SECRET,
                code);

        // 发送请求，获取assess_token
        String result = null;
        try {
            result = HttpClientUtils.get(accessTokenUrl);
            log.info("result:" + result);
            // 将返回值，转化为对象，方便取值
            JSONObject resultObject = JSONObject.parseObject(result);
            String accessToken = resultObject.getString("access_token");
            String openId = resultObject.getString("openid");
            //log.info("access_token：" + accessToken);
            //log.info("openid：" + openId);
            //log.info("refresh_token：" + refreshToken);
            if (resultObject.getString("errcode") != null) {
                log.error("获取access_token失败：" + resultObject.getString("errcode") + resultObject.getString("errmsg"));
                throw new BytException(ResultCodeEnum.FETCH_ACCESSTOKEN_FAILD);
            }

            //"https://api.weixin.qq.com/sns/userinfo?access_token=accessToken&openid=openId";
            // 发送请求获取扫描人信息
            String baseUserInfoUrl = "https://api.weixin.qq.com/sns/userinfo" +
                    "?access_token=%s" +
                    "&openid=%s";
            String userInfoUrl = String.format(baseUserInfoUrl, accessToken, openId);
            String userInfo = HttpClientUtils.get(userInfoUrl);
            log.info("扫描人信息，userInfo：" + userInfo);
            JSONObject userInfoObject = JSONObject.parseObject(userInfo);
            // 获取扫描人信息失败
            if (userInfoObject.getString("errcode") != null) {
                log.error("获取用户信息失败：" + userInfoObject.getString("errcode") + userInfoObject.getString("errmsg"));
                throw new BytException(ResultCodeEnum.FETCH_USERINFO_ERROR);
            }
            String nickName = userInfoObject.getString("nickname");
            String headImgrl = userInfoObject.getString("headimgurl");
            // 保存扫描人信息
            UserInfo info = new UserInfo();
            info.setOpenid(openId);
            info.setNickName(nickName);
            info.setStatus(1);
            // 判断是否存在，当前扫描人
            boolean isUserInfo = userInfoService.selectUserInfoByopenId(openId);
            //log.info(" isUserInfo "+ isUserInfo);
            if (isUserInfo) userInfoService.save(info);
            // 封装值，返回前端，进行显示
            Map<String, Object> map = new HashMap<>();
            if (StringUtils.isEmpty(info.getName())) {
                // 设置name值
                map.put("name", nickName);
            }
            // 如果name,还为空，则将手机号给name
            if (StringUtils.isEmpty(info.getName()) && !StringUtils.isEmpty(info.getPhone())) {
                map.put("name", info.getPhone());
            }
            // 当没有手机号时，则是扫描登录，保存openId
            if (StringUtils.isEmpty(info.getPhone())) {
                map.put("openid", openId);
            } else {
                map.put("openid", "");
            }
            String token = JwtUtil.createToken(info.getId(), info.getName());
            map.put("token", token);
            // 重定向到前端
            return "redirect:http://localhost:3000/weixin/callback?token="
                    + map.get("token") + "&openid=" + map.get("openid")
                    + "&name=" + URLEncoder.encode((String) map.get("name"),"utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
