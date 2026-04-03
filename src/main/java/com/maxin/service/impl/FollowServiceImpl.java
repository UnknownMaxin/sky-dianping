package com.maxin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maxin.constant.RedisConstant;
import com.maxin.dto.UserDTO;
import com.maxin.entity.Follow;
import com.maxin.mapper.FollowMapper;
import com.maxin.service.FollowService;
import com.maxin.service.UserService;
import com.maxin.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements FollowService {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 关注/取关用户
     * @param followId
     * @param isFollow
     */
    public void follow(Long followId, Boolean isFollow) {
        Long userId = UserHolder.getUser().getId();

        // 判断是关注还是取关
        String key = RedisConstant.FOLLOW_KEY + userId;
        if (isFollow) {
            Follow follow = new Follow();
            follow.setUserId(userId);
            follow.setFollowUserId(followId);
            boolean saved = save(follow);
            if (saved) {
                // 共同关注
                redisTemplate.opsForSet().add(key, followId.toString());
            }
        } else {
            boolean removed = remove(new QueryWrapper<Follow>()
                    .eq("user_id", userId).eq("follow_user_id", followId));
            if (removed) {
                // 移除共同关注
                redisTemplate.opsForSet().remove(key, followId.toString());
            }
        }
    }

    /**
     * 查询是否关注此用户
     * @param followId
     * @return
     */
    public Boolean isFollow(Long followId) {
        Long userId = UserHolder.getUser().getId();
        Long count = query().eq("user_id", userId).eq("follow_user_id", followId).count();
        return count > 0;
    }

    /**
     * 共同关注用户
     * @param id
     * @return
     */
    public List<UserDTO> followCommons(Long id) {
        Long userId = UserHolder.getUser().getId();
        String key = RedisConstant.FOLLOW_KEY + userId, key2 = RedisConstant.FOLLOW_KEY + id;

        // 求交集
        Set<String> intersect = redisTemplate.opsForSet().intersect(key, key2);
        if (intersect == null || intersect.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> ids = intersect.stream().map(Long::valueOf).collect(Collectors.toList());
        List<UserDTO> userDTOList = userService.listByIds(ids)
                .stream()
                .map(user -> BeanUtil.copyProperties(user, UserDTO.class))
                .collect(Collectors.toList());
        return userDTOList;
    }

}
