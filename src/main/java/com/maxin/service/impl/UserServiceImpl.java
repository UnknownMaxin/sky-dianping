package com.maxin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.maxin.constant.MessageConstant;
import com.maxin.constant.RedisConstant;
import com.maxin.dto.LoginFormDTO;
import com.maxin.dto.UserDTO;
import com.maxin.entity.User;
import com.maxin.exception.PhoneInvalidException;
import com.maxin.exception.VerificationCodeException;
import com.maxin.mapper.UserMapper;
import com.maxin.service.UserService;
import com.maxin.utils.RegexUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;


@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 用户登录
     * @param loginFormDTO
     * @param session
     */
    public void login(LoginFormDTO loginFormDTO, HttpSession session) {
        if (RegexUtils.isPhoneInvalid(loginFormDTO.getPhone())) {
            throw new PhoneInvalidException(MessageConstant.PHONE_INVALID);
        }

        Object cacheCode = redisTemplate.opsForValue().get(RedisConstant.LOGIN_CODE_KEY + loginFormDTO.getPhone());
        //Object cacheCode = session.getAttribute("code");
        String code = loginFormDTO.getCode();
        if (cacheCode == null || !cacheCode.equals(code)) {
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
        // 教程里这办法好麻烦，以后自己搓的话还是用RedisTemplate吧
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString()));
        String tokenKey = RedisConstant.LOGIN_USER_KEY + token;
        redisTemplate.opsForHash().putAll(tokenKey, userMap);
        redisTemplate.expire(tokenKey, RedisConstant.LOGIN_USER_TTL, TimeUnit.MINUTES);
        //session.setAttribute("user", user);
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
        log.debug("短信验证码：" + code);
    }

    private User createUserWithPhone(String phone) {
        User user = new User();
        user.setPhone(phone);
        user.setNickName("user_" + RandomUtil.randomString(10));
        save(user);
        return user;
    }
}
