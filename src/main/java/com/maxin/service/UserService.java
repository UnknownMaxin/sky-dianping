package com.maxin.service;

import com.maxin.dto.LoginFormDTO;
import com.maxin.entity.UserInfo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public interface UserService {

    /**
     * 用户登录
     * @param loginFormDTO
     * @param session
     */
    void login(LoginFormDTO loginFormDTO, HttpSession session);

    /**
     * 发送验证码
     * @param phone
     * @param session
     */
    void sendCode(String phone, HttpSession session);

    /**
     * 退出登录
     * @param request
     */
    void logout(HttpServletRequest request);

    /**
     * 获取用户详情
     * @param userId
     * @return
     */
    UserInfo getUserInfo(Long userId);
}
