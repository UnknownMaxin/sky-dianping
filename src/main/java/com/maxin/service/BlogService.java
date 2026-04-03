package com.maxin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.maxin.dto.UserDTO;
import com.maxin.entity.Blog;
import com.maxin.result.ScrollResult;

import java.util.List;

public interface BlogService extends IService<Blog> {

    /**
     * 新增笔记
     * @param blog
     */
    void saveBlog(Blog blog);

    /**
     * 点赞笔记
     * @param id
     */
    void likeBlog(Long id);

    /**
     * 查找我的笔记
     * @param current
     * @return
     */
    List<Blog> queryMyBlog(Integer current);

    /**
     * 查找热门笔记
     * @param current
     * @return
     */
    List<Blog> queryHotBlog(Integer current);

    /**
     * 根据id查找笔记
     *
     * @param id
     * @return
     */
    Blog queryBlogById(Long id);

    /**
     * 查询top5的点赞用户
     * @param id
     * @return
     */
    List<UserDTO> queryBlogLikes(Long id);

    /**
     * 查询关注用户的笔记
     *
     * @param max
     * @param offset
     * @return
     */
    ScrollResult queryBlogOfFollow(Long max, Integer offset);
}
