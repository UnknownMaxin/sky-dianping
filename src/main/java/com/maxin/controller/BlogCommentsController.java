package com.maxin.controller;


import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/blog-comments")
@Tag(name = "博客评论相关接口")
@Slf4j
public class BlogCommentsController {

}
