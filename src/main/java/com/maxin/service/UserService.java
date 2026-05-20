package com.maxin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.maxin.dto.LoginFormDTO;
import com.maxin.entity.User;
import com.maxin.entity.UserInfo;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public interface UserService extends IService<User> {

    /**
     * 用户登录
     *
     * @param loginFormDTO
     * @param session
     * @return
     */
    String login(LoginFormDTO loginFormDTO, HttpSession session);

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
