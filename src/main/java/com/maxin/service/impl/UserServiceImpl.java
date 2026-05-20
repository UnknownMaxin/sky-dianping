package com.maxin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.maxin.constant.MessageConstant;
import com.maxin.constant.RedisConstant;
import com.maxin.dto.LoginFormDTO;
import com.maxin.dto.UserDTO;
import com.maxin.entity.User;
import com.maxin.entity.UserInfo;
import com.maxin.exception.PhoneInvalidException;
import com.maxin.exception.VerificationCodeException;
import com.maxin.mapper.UserInfoMapper;
import com.maxin.mapper.UserMapper;
import com.maxin.service.UserService;
import com.maxin.utils.RegexUtils;
import com.maxin.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;


@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 用户登录
     *
     * @param loginFormDTO
     * @param session
     * @return
     */
    public String login(LoginFormDTO loginFormDTO, HttpSession session) {
        if (RegexUtils.isPhoneInvalid(loginFormDTO.getPhone())) {
            throw new PhoneInvalidException(MessageConstant.PHONE_INVALID);
        }

        Object cacheCode = redisTemplate.opsForValue().get(RedisConstant.LOGIN_CODE_KEY + loginFormDTO.getPhone());
        String code = loginFormDTO.getCode();
        if (cacheCode == null || !cacheCode.toString().equals(code)) {
            throw new VerificationCodeException(MessageConstant.INCORRECT_VERIFICATION_CODE);
        }

        // MyBatisPlus真离谱啊
        User user = query().eq("phone", loginFormDTO.getPhone()).one();
        if (user == null) {
            user = createUserWithPhone(loginFormDTO.getPhone());
        }

        String token = UUID.randomUUID().toString();
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);

        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString()));
        String tokenKey = RedisConstant.LOGIN_USER_KEY + token;
        redisTemplate.opsForHash().putAll(tokenKey, userMap);
        redisTemplate.expire(tokenKey, RedisConstant.LOGIN_USER_TTL, TimeUnit.MINUTES);
        //session.setAttribute("user", user);

        return token;
    }

    /**
     * 发送验证码
     * @param phone
     * @param session
     */
    public void sendCode(String phone, HttpSession session) {
        if (RegexUtils.isPhoneInvalid(phone)) {
            throw new PhoneInvalidException(MessageConstant.PHONE_INVALID);
        }

        String code = RandomUtil.randomNumbers(6);
        //session.setAttribute("code", code);
        redisTemplate.opsForValue().set(RedisConstant.LOGIN_CODE_KEY + phone, code, RedisConstant.LOGIN_CODE_TTL, TimeUnit.MINUTES);

        // TODO：发送验证码功能
        log.info("短信验证码：" + code);
    }

    /**
     * 退出登录
     * @param request
     */
    public void logout(HttpServletRequest request) {
        String token = request.getHeader("authorization");
        if (StrUtil.isNotBlank(token)) {
            String tokenKey = RedisConstant.LOGIN_USER_KEY + token;
            redisTemplate.delete(tokenKey);
        }
        UserHolder.removeUser();
    }

    /**
     * 获取用户详情
     * @param userId
     * @return
     */
    public UserInfo getUserInfo(Long userId) {
        UserInfo userInfo = userInfoMapper.getById(userId);
        if (userInfo == null) {
            return null;
        }
        // 说实话，我没搞懂原项目为什么要这么写？？？
        userInfo.setCreateTime(LocalDateTime.now());
        userInfo.setUpdateTime(LocalDateTime.now());
        return userInfo;
    }

    /**
     * 根据手机号创建新用户
     * @param phone
     * @return
     */
    private User createUserWithPhone(String phone) {
        User user = new User();
        user.setPhone(phone);
        user.setNickName("user_" + RandomUtil.randomString(10));
        save(user);
        return user;
    }
}
