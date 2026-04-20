package com.maxin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.maxin.constant.SystemConstant;
import com.maxin.dto.UserDTO;
import com.maxin.result.Result;
import com.maxin.entity.Blog;
import com.maxin.result.ScrollResult;
import com.maxin.service.BlogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/blog")
@Tag(name = "笔记相关接口")
@Slf4j
public class BlogController {

    @Resource
    private BlogService blogService;

    /**
     * 新增笔记
     * @param blog
     * @return
     */
    @PostMapping
    @Operation(summary = "新增笔记")
    public Result<Long> saveBlog(@RequestBody Blog blog) {
        blogService.saveBlog(blog);
        return Result.success(blog.getId());
    }

    /**
     * 点赞笔记
     * @param id
     * @return
     */
    @PutMapping("/like/{id}")
    @Operation(summary = "点赞笔记")
    public Result likeBlog(@PathVariable("id") Long id) {
        blogService.likeBlog(id);
        return Result.success();
    }

    /**
     * 查找笔记
     * @param current
     * @return
     */
    @GetMapping("/of/me")
    @Operation(summary = "查找笔记")
    public Result<List<Blog>> queryMyBlog(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        List<Blog> records = blogService.queryMyBlog(current);
        return Result.success(records);
    }

    /**
     * 查找热门笔记
     * @param current
     * @return
     */
    @GetMapping("/hot")
    @Operation(summary = "查找热门笔记")
    public Result<List<Blog>> queryHotBlog(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        List<Blog> records = blogService.queryHotBlog(current);
        return Result.success(records);
    }

    /**
     * 根据id查找笔记
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据id查找笔记")
    public Result<Blog> queryBlogById(@PathVariable("id") Long id) {
        Blog records = blogService.queryBlogById(id);
        return Result.success(records);
    }

    /**
     * 查询top5的点赞用户
     * @param id
     * @return
     */
    @GetMapping("/likes/{id}")
    @Operation(summary = "查询top5的点赞用户")
    public Result<List<UserDTO>> queryBlogLikes(@PathVariable("id") Long id) {
        List<UserDTO> records = blogService.queryBlogLikes(id);
        return Result.success(records);
    }

    /**
     * 根据用户id查询笔记
     * @param current
     * @param id
     * @return
     */
    @GetMapping("/of/user")
    @Operation(summary = "根据用户id查询笔记")
    public Result<List<Blog>> queryBlogByUserId(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam("id") Long id) {
        // 根据用户查询
        Page<Blog> page = blogService.query()
                .eq("user_id", id).page(new Page<>(current, SystemConstant.MAX_PAGE_SIZE));
        // 获取当前页数据
        List<Blog> records = page.getRecords();
        return Result.success(records);
    }

    /**
     * 查询关注用户的笔记
     * @param max
     * @param offset
     * @return
     */
    @GetMapping("/of/follow")
    @Operation(summary = "查询关注用户的笔记")
    public Result<ScrollResult> queryBlogOfFollow(
            @RequestParam("lastId") Long max, @RequestParam(value = "offset", defaultValue = "0") Integer offset){
        ScrollResult result = blogService.queryBlogOfFollow(max, offset);
        return Result.success(result);
    }
}
