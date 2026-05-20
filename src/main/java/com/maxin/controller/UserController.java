package com.maxin.controller;

import com.maxin.dto.LoginFormDTO;
import com.maxin.dto.UserDTO;
import com.maxin.entity.UserInfo;
import com.maxin.result.Result;
import com.maxin.service.UserService;
import com.maxin.utils.UserHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/user")
@Tag(name = "用户相关接口")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户登录
     * @param loginFormDTO
     * @param session
     * @return
     */
    @PostMapping("/login")
    @Operation(summary = "账号登录")
    public Result<String> login(@RequestBody LoginFormDTO loginFormDTO, HttpSession session) {
        String token = userService.login(loginFormDTO, session);
        return Result.success(token);
    }

    /**
     * 发送验证码
     * @param phone
     * @param session
     * @return
     */
    @PostMapping("/code")
    @Operation(summary = "发送手机验证码")
    public Result sendCode(@RequestParam("phone") String phone, HttpSession session) {
        userService.sendCode(phone, session);
        return Result.success();
    }

    /**
     * 退出登录
     * @param request
     * @return
     */
    @PostMapping("/logout")
    @Operation(summary = "退出登录")
    public Result logout(HttpServletRequest request) {
        userService.logout(request);
        return Result.success();
    }

    /**
     * 获取用户
     * @return
     */
    @GetMapping("/me")
    public Result<UserDTO> me() {
        UserDTO userDTO = UserHolder.getUser();
        return Result.success(userDTO);
    }

    /**
     * 获取用户详情
     * @param userId
     * @return
     */
    @GetMapping("/info/{id}")
    public Result<UserInfo> info(@PathVariable("id") Long userId) {
        return Result.success(userService.getUserInfo(userId));
    }


}
