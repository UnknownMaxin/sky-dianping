package com.maxin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.maxin.dto.UserDTO;
import com.maxin.entity.Follow;

import java.util.List;

public interface FollowService extends IService<Follow> {

    /**
     * 关注/取关用户
     * @param followId
     * @param isFollow
     */
    void follow(Long followId, Boolean isFollow);

    /**
     * 查询是否关注此用户
     * @param followId
     * @return
     */
    Boolean isFollow(Long followId);

    /**
     * 共同关注用户
     * @param id
     * @return
     */
    List<UserDTO> followCommons(Long id);
}
