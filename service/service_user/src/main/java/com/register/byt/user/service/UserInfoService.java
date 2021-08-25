package com.register.byt.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.register.model.entity.user.UserInfo;
import com.register.model.vo.user.LoginVo;
import com.register.model.vo.user.RegisterVo;
import com.register.model.vo.user.UserAuthVo;
import com.register.model.vo.user.UserInfoQueryVo;

import java.util.Map;

/**
 * @author LLXX
 * @create 2021-08-11 16:19
 */
public interface UserInfoService extends IService<UserInfo> {

    /**
     * 用户登录
     * @param loginVo
     * @return
     */
    Map<String, Object> login(LoginVo loginVo);

    /**
     * 判断是否存在当前扫描人
     * @param openId
     * @return
     */
    boolean selectUserInfoByopenId(String openId);

    /**
     * 用户注册
     * @param registerVo
     * @return
     */
    boolean register(RegisterVo registerVo);

    /**
     * 用户认证
     * @param userId
     * @param userAuthVo
     */
    void userAuth(Long userId, UserAuthVo userAuthVo);

    /**
     * 分页获取用户列表
     * @param infoPage 分页对象
     * @param queryVo  查询条件
     * @return
     */
    IPage<UserInfo> selectPage(Page<UserInfo> infoPage, UserInfoQueryVo queryVo);

    /**
     * 用户锁定
     * @param userId
     * @param status
     */
    void lock(Long userId, Integer status);

    /**
     * 获取用户详细
     * @param userId 用户ID
     * @return
     */
    Map<String, Object> showUserInfo(Long userId);

    /**
     * 用户审批
     * @param userId 用户ID
     * @param authStatus 审批状态码
     */
    void approval(Long userId, Integer authStatus);
}
