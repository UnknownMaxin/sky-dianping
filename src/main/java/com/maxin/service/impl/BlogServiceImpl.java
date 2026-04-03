package com.maxin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maxin.constant.MessageConstant;
import com.maxin.constant.RedisConstant;
import com.maxin.constant.SystemConstant;
import com.maxin.dto.UserDTO;
import com.maxin.entity.Blog;
import com.maxin.entity.Follow;
import com.maxin.entity.User;
import com.maxin.exception.QueryBlogException;
import com.maxin.exception.SaveBlogException;
import com.maxin.mapper.BlogMapper;
import com.maxin.result.ScrollResult;
import com.maxin.service.BlogService;
import com.maxin.service.FollowService;
import com.maxin.service.UserService;
import com.maxin.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements BlogService {

    @Autowired
    private FollowService followService;
    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 新增笔记
     * @param blog
     */
    public void saveBlog(Blog blog) {
        UserDTO user = UserHolder.getUser();
        blog.setUserId(user.getId());

        boolean saved = save(blog);
        if (!saved) {
            throw new SaveBlogException(MessageConstant.SAVE_BLOG_FAILED);
        }

        // 推送给所有关注此笔记作者的粉丝，Feed流
        List<Follow> follows = followService.query().eq("follow_user_id", user.getId()).list();
        follows.forEach(follow -> {
            Long userId = follow.getUserId();
            String key = RedisConstant.FEED_KEY + userId;
            redisTemplate.opsForZSet().add(key, blog.getId().toString(), System.currentTimeMillis());
        });
    }

    /**
     * 点赞笔记
     * @param id
     */
    public void likeBlog(Long id) {
        Long userId = UserHolder.getUser().getId();

        String key = RedisConstant.BLOG_LIKED_KEY + userId;
        Double score = redisTemplate.opsForZSet().score(key, userId.toString());
        if (score == null) {
            // 未点赞，则可以点赞
            boolean updated = update().setSql("liked = liked + 1").eq("id", id).update();
            if (updated) {
                redisTemplate.opsForZSet().add(key, userId.toString(), System.currentTimeMillis());
            }
        } else {
            // 已点赞，则取消点赞
            boolean updated = update().setSql("liked = liked - 1").eq("id", id).update();
            if (updated) {
                redisTemplate.opsForZSet().remove(key, userId.toString());
            }
        }
    }

    /**
     * 查找我的笔记
     * @param current
     * @return
     */
    public List<Blog> queryMyBlog(Integer current) {
        // 全部用户的笔记存到Redis不现实，直接读数据库得了
        UserDTO user = UserHolder.getUser();
        Page<Blog> page = query().eq("user_id", user.getId()).page(new Page<>(current, SystemConstant.MAX_PAGE_SIZE));
        return page.getRecords();
    }

    /**
     * 查找热门笔记
     * @param current
     * @return
     */
    public List<Blog> queryHotBlog(Integer current) {
        Page<Blog> page = query().orderByDesc("liked").page(new Page<>(current, SystemConstant.MAX_PAGE_SIZE));

        List<Blog> records = page.getRecords();
        records.forEach(blog -> {
            this.queryBlogUser(blog);
            this.isBlogLiked(blog);
        });
        return records;
    }

    /**
     * 根据id查找笔记
     * @param id
     * @return
     */
    public Blog queryBlogById(Long id) {
        Blog blog = getById(id);
        if (blog == null) {
            throw new QueryBlogException(MessageConstant.BLOG_IS_NOT_EXIST);
        }
        queryBlogUser(blog);
        isBlogLiked(blog);
        return blog;
    }

    /**
     * 查询top5的点赞用户
     * @param id
     * @return
     */
    public List<UserDTO> queryBlogLikes(Long id) {
        String key = RedisConstant.BLOG_LIKED_KEY + id;

        Set<String> top5 = redisTemplate.opsForZSet().range(key, 0, 4);
        if (top5 == null || top5.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> ids = top5.stream().map(Long::valueOf).collect(Collectors.toList());
        String idStr = StrUtil.join(",", ids);
        List<UserDTO> userDTOList = userService.query()
                .in("id", ids).last("ORDER BY FIELD(id," + idStr + ")").list()
                .stream()
                .map(user -> BeanUtil.copyProperties(user, UserDTO.class))
                .collect(Collectors.toList());

        return userDTOList;
    }

    /**
     * 查询关注用户的笔记
     * @param max
     * @param offset
     * @return
     */
    public ScrollResult queryBlogOfFollow(Long max, Integer offset) {
        Long userId = UserHolder.getUser().getId();

        // 查询收件箱
        String key = RedisConstant.FEED_KEY + userId;
        Set<ZSetOperations.TypedTuple<String>> typedTuples = redisTemplate.opsForZSet()
                .reverseRangeByScoreWithScores(key, 0, max, offset, 2);
        if (typedTuples == null || typedTuples.isEmpty()) {
            return new ScrollResult();
        }

        // 解析数据：blogId、minTime（时间戳）、offset
        List<Long> ids = new ArrayList<>(typedTuples.size());
        long minTime = 0; // 2
        int os = 1; // 2
        for (ZSetOperations.TypedTuple<String> tuple : typedTuples) { // 5 4 4 2 2
            ids.add(Long.valueOf(tuple.getValue()));
            Long time = tuple.getScore().longValue();
            if (time == minTime) {
                os++;
            } else {
                minTime = time;
                os = 1;
            }
        }

        // 根据id查询blog
        String idStr = StrUtil.join(",", ids);
        List<Blog> blogs = query().in("id", ids).last("ORDER BY FIELD(id," + idStr + ")").list();
        blogs.forEach(blog -> {
            queryBlogUser(blog);
            isBlogLiked(blog);
        });

        ScrollResult r = new ScrollResult();
        r.setList(blogs);
        r.setOffset(os);
        r.setMinTime(minTime);
        return r;
    }

    /**
     * 查找笔记作者
     * @param blog
     */
    private void queryBlogUser(Blog blog) {
        Long userId = blog.getUserId();
        User user = userService.getById(userId);
        blog.setName(user.getNickName());
        blog.setIcon(user.getIcon());
    }

    /**
     * 获取用户是否点赞此笔记
     * @param blog
     */
    private void isBlogLiked(Blog blog) {
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            // 用户未登录，无需查询是否点赞
            return;
        }
        Long userId = user.getId();

        String key = RedisConstant.BLOG_LIKED_KEY + blog.getId();
        Double score = redisTemplate.opsForZSet().score(key, userId.toString());
        blog.setIsLike(score != null);
    }
}
