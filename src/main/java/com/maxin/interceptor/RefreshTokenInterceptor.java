package com.maxin.interceptor;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.maxin.dto.UserDTO;
import com.maxin.constant.RedisConstant;
import com.maxin.utils.UserHolder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RefreshTokenInterceptor implements HandlerInterceptor {

    private RedisTemplate redisTemplate;

    public RefreshTokenInterceptor(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("authorization");
        if (StrUtil.isBlank(token)) {
            return true;
        }

        // 获取hash
        String tokenKey = RedisConstant.LOGIN_USER_KEY + token;
        Map<Object, Object> userMap = redisTemplate.opsForHash().entries(tokenKey);
        if (userMap.isEmpty()) {
            return true;
        }

        // 将hash数据转化为UserDTO
        UserDTO userDTO = new UserDTO();
        BeanUtil.fillBeanWithMap(userMap, userDTO, false);
        UserHolder.saveUser(userDTO);

        redisTemplate.expire(tokenKey, RedisConstant.LOGIN_USER_TTL, TimeUnit.MINUTES);

//        HttpSession session = request.getSession();
//
//        Object user = session.getAttribute("user");
//        if (user == null) {
//            response.setStatus(401);
//            return false;
//        }
//        // 转换对象
//        UserDTO userDTO = new UserDTO();
//        BeanUtils.copyProperties(user, userDTO);
//        UserHolder.saveUser(userDTO);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserHolder.removeUser();
    }
}
