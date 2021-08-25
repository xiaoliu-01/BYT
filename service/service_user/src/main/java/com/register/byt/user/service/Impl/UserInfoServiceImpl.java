package com.register.byt.user.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.register.byt.commons.result.ResultCodeEnum;
import com.register.byt.commons.utils.JwtUtil;
import com.register.byt.exception.BytException;
import com.register.byt.user.mapper.UserInfoMapper;
import com.register.byt.user.service.PatientService;
import com.register.byt.user.service.UserInfoService;
import com.register.model.entity.user.Patient;
import com.register.model.entity.user.UserInfo;
import com.register.model.enums.AuthStatusEnum;
import com.register.model.vo.user.LoginVo;
import com.register.model.vo.user.RegisterVo;
import com.register.model.vo.user.UserAuthVo;
import com.register.model.vo.user.UserInfoQueryVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author LLXX
 * @create 2021-08-11 16:20
 */
@Service
@Slf4j
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private PatientService patientService;

    @Override
    public Map<String, Object> login(LoginVo loginVo) {
        QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
        String code = loginVo.getCode();
        String phone = loginVo.getPhone();
        // 当code与手机号为空时，直接抛出异常
        if (StringUtils.isEmpty(code) || StringUtils.isEmpty(phone)) {
            log.warn("参数不正确");
            throw new BytException(ResultCodeEnum.PARAM_ERROR.getMessage());
        }
        // 校验校验验证码
        String phoneCode = redisTemplate.opsForValue().get("phone");
        log.info("code中的验证码为" + code);
        log.info("redis中的验证码为" + phoneCode);
        if (!code.equals(phoneCode)) {
            throw new BytException(ResultCodeEnum.CODE_ERROR);
        }
        // 绑定手机号码
        UserInfo userInfo = null;
        if (!StringUtils.isEmpty(loginVo.getOpenid())) {
            QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("openid", loginVo.getOpenid());
            userInfo = this.getOne(queryWrapper);
            // 当userInfo不为空时，则设置手机号，更新userInfo数据
            if (userInfo != null) {
                userInfo.setPhone(loginVo.getPhone());
                // 更新
                this.updateById(userInfo);
            } else {
                throw new BytException(ResultCodeEnum.DATA_ERROR);
            }
        }
        // 用户是否存在
        wrapper.eq("phone", phone);
        userInfo = this.getOne(wrapper);
        if (userInfo == null) { // 手机号未注册
            log.warn("手机号未注册");
            RegisterVo registerVo = new RegisterVo();
            registerVo.setMobile(loginVo.getPhone());
            this.register(registerVo);
        }
        // 校验是否被禁用
        if (userInfo.getStatus() == 0) {
            log.warn("该用户已被禁用");
            throw new BytException(ResultCodeEnum.LOGIN_DISABLED_ERROR.getMessage());
        }
        // 返回页面显示名称
        HashMap<String, Object> map = new HashMap<>();
        String name = userInfo.getName();
        if (!StringUtils.isEmpty(name)) map.put("name", name);
        // 设置token
        String token = JwtUtil.createToken(userInfo.getId(), userInfo.getName());
        map.put("token", token);
        return map;
    }

    @Override
    public boolean selectUserInfoByopenId(String openId) {
        QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
        wrapper.eq(!StringUtils.isEmpty(openId), "openid", openId);
        UserInfo userInfo = baseMapper.selectOne(wrapper);
        return userInfo == null;
    }

    public boolean register(RegisterVo registerVo) {
        UserInfo userInfo = new UserInfo();
        userInfo.setPhone(registerVo.getMobile());
        userInfo.setNickName(registerVo.getMobile());
        userInfo.setName(registerVo.getMobile());
        userInfo.setStatus(1);
        return this.save(userInfo);
    }

    @Override
    public void userAuth(Long userId, UserAuthVo userAuthVo) {
        UserInfo userInfo = this.getById(userId);
        userInfo.setCertificatesNo(userAuthVo.getCertificatesNo());
        userInfo.setCertificatesType(userAuthVo.getCertificatesType());
        userInfo.setCertificatesUrl(userAuthVo.getCertificatesUrl());
        userInfo.setAuthStatus(AuthStatusEnum.AUTH_RUN.getStatus());
        this.updateById(userInfo);
    }

    @Override
    public IPage<UserInfo> selectPage(Page<UserInfo> infoPage, UserInfoQueryVo queryVo) {
        Integer authStatus = queryVo.getAuthStatus(); // 认证状态
        String createTimeBegin = queryVo.getCreateTimeBegin(); // 开始时间
        String createTimeEnd = queryVo.getCreateTimeEnd();  // 结束时间
        Integer status = queryVo.getStatus();   // 用户状态
        String keyword = queryVo.getKeyword();  // 查询关键字
        QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
        wrapper.like(!StringUtils.isEmpty(keyword), "name", keyword)
                .ge(!StringUtils.isEmpty(createTimeBegin), "create_time", createTimeBegin)
                .le(!StringUtils.isEmpty(createTimeBegin), "create_time", createTimeEnd)
                .eq(status != null, "status", status)
                .eq(authStatus != null , "auth_status" ,authStatus);
        Page<UserInfo> page = this.page(infoPage, wrapper);
        List<UserInfo> userInfos = page.getRecords();
        userInfos.stream().forEach(userInfo -> {
            packageUserInfo(userInfo);
        });
        return page;
    }

    @Override
    public void lock(Long userId, Integer status) {
        if (status.intValue() ==  0 || status.intValue() == 1){
            UserInfo userInfo = this.getById(userId);
            userInfo.setStatus(status);
            this.updateById(userInfo);
        }
    }

    @Override
    public Map<String, Object> showUserInfo(Long userId) {
        HashMap<String, Object> map = new HashMap<>();
        UserInfo userInfo = this.getById(userId);
        // 用户信息
        userInfo = packageUserInfo(userInfo);
        // 就诊人信息
        List<Patient> patients = patientService.findAllUserId(userId);
        map.put("userInfo",userInfo);
        map.put("patientList",patients);
        return map;
    }

    @Override
    public void approval(Long userId, Integer authStatus) {
        // 认证审批  2通过  -1不通过
        if(authStatus.intValue() == 2 || authStatus.intValue() == -1){
            UserInfo userInfo = getById(userId);
            userInfo.setAuthStatus(authStatus);
            updateById(userInfo);
        }
    }

    // 编号变成对应值封装
    private UserInfo packageUserInfo(UserInfo userInfo) {
        String statusNameByStatus = AuthStatusEnum.getStatusNameByStatus(userInfo.getAuthStatus());
        //处理用户状态 0  1
        String statusString  = userInfo.getStatus().intValue() == 0 ? "锁定":"正常";
        userInfo.getParam().put("authStatusString",statusNameByStatus);
        userInfo.getParam().put("statusString",statusString);
        return userInfo;
    }
}
