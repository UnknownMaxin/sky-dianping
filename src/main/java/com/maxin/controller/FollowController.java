package com.maxin.controller;


import com.maxin.dto.UserDTO;
import com.maxin.result.Result;
import com.maxin.service.FollowService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/follow")
@Api(tags = "关注相关接口")
@Slf4j
public class FollowController {

    @Autowired
    private FollowService followService;

    /**
     * 关注/取关用户
     * @param followId
     * @param isFollow
     * @return
     */
    @PutMapping("/{id}/{isFollow}")
    @ApiOperation("关注/取关用户")
    public Result follow(@PathVariable("id") Long followId, @PathVariable("isFollow") Boolean isFollow) {
        followService.follow(followId, isFollow);
        return Result.success();
    }

    /**
     * 查询是否关注此用户
     * @param followId
     * @return
     */
    @GetMapping("/or/not/{id}")
    @ApiOperation("查询是否关注用户")
    public Result<Boolean> isFollow(Long followId) {
        Boolean isFollow = followService.isFollow(followId);
        return Result.success(isFollow);
    }

    /**
     * 共同关注用户
     * @param id
     * @return
     */
    @GetMapping("/common/{id}")
    @ApiOperation("共同关注用户")
    public Result<List<UserDTO>> followCommons(Long id) {
        List<UserDTO> results = followService.followCommons(id);
        return Result.success(results);
    }
}
