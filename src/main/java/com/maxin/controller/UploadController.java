package com.maxin.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.maxin.constant.MessageConstant;
import com.maxin.exception.DeleteBlogImgException;
import com.maxin.exception.UploadException;
import com.maxin.result.Result;
import com.maxin.constant.SystemConstant;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/upload")
@Api(tags = "上传文件相关接口")
@Slf4j
public class UploadController {

    /**
     * 上传博客图片
     * @param image
     * @return
     */
    @PostMapping("/blog")
    @ApiOperation("上传博客图片")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile image) {
        try {
            // 获取原始文件名称
            String originalFilename = image.getOriginalFilename();
            // 生成新文件名
            String fileName = createNewFileName(originalFilename);
            // 保存文件
            image.transferTo(new File(SystemConstant.IMAGE_UPLOAD_DIR, fileName));
            // 返回结果
            log.debug("文件上传成功，{}", fileName);
            return Result.success(fileName);
        } catch (IOException e) {
            throw new UploadException(MessageConstant.UPLOAD_FAILED);
        }
    }

    /**
     * 删除博客图片
     * @param filename
     * @return
     */
    @GetMapping("/blog/delete")
    @ApiOperation("删除博客图片")
    public Result deleteBlogImg(@RequestParam("name") String filename) {
        File file = new File(SystemConstant.IMAGE_UPLOAD_DIR, filename);
        if (file.isDirectory()) {
            throw new DeleteBlogImgException(MessageConstant.INCORRECT_FILE_NAME);
        }
        FileUtil.del(file);
        return Result.success();
    }

    /**
     * 生成文件名
     * @param originalFilename
     * @return
     */
    private String createNewFileName(String originalFilename) {
        // 获取后缀
        String suffix = StrUtil.subAfter(originalFilename, ".", true);
        // 生成目录
        String name = UUID.randomUUID().toString();
        int hash = name.hashCode();
        int d1 = hash & 0xF;
        int d2 = (hash >> 4) & 0xF;
        // 判断目录是否存在
        File dir = new File(SystemConstant.IMAGE_UPLOAD_DIR, StrUtil.format("/blogs/{}/{}", d1, d2));
        if (!dir.exists()) {
            dir.mkdirs();
        }
        // 生成文件名
        return StrUtil.format("/blogs/{}/{}/{}.{}", d1, d2, name, suffix);
    }
}
