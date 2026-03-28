package com.maxin.controller;

import com.maxin.dto.LoginFormDTO;
import com.maxin.dto.UserDTO;
import com.maxin.entity.UserInfo;
import com.maxin.result.Result;
import com.maxin.service.UserService;
import com.maxin.utils.UserHolder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/user")
@Api(tags = "用户相关接口")
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
    @ApiOperation("账号登录")
    public Result login(@RequestBody LoginFormDTO loginFormDTO, HttpSession session) {
        userService.login(loginFormDTO, session);
        return Result.success();
    }

    /**
     * 发送验证码
     * @param phone
     * @param session
     * @return
     */
    @PostMapping("/code")
    @ApiOperation("发送手机验证码")
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
    @ApiOperation("退出登录")
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
