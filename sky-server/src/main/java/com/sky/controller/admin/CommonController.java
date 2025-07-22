package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.swing.*;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/admin/common")
@Api(tags = "通用接口")
@Slf4j
public class CommonController {
    @Autowired
    private AliOssUtil aliOssUtil;

    /**
     * 文件上传
     */
    @PostMapping("/upload")
    @ApiOperation("文件上传")
   /*在 Java Web 开发中，MultipartFile 是 Spring 框架提供的一个接口，用于处理 HTTP 请求中的文件上传。
    它封装了上传文件的相关信息和操作方法，简化了文件上传的处理流程。*/
    public Result<String> upload(MultipartFile file) {
        log.info("文件上传：{}", file);
        try {
            //原始文件名
            String originalFilename = file.getOriginalFilename();
            //截取原始文件名的后缀  afaf.png
            /* 1、substring(int beginIndex, int endIndex)
              作用：截取从beginIndex（包含）到endIndex（不包含）之间的子字符串。
              2、substring(int beginIndex)
              从指定索引beginIndex开始，截取到字符串末尾的子字符串。
              indexOf(int ch)
              作用：查找字符（Unicode 码点）在字符串中首次出现的位置。*/
            String substring = originalFilename.substring(originalFilename.indexOf("."));
            //构造新文件名
            String objectName = UUID.randomUUID().toString() + substring;

            String filePath = aliOssUtil.upload(file.getBytes(), objectName);
            return Result.success(filePath);
        } catch (IOException e) {
            log.error("文件上传失败：{}", e);
        }
        return Result.error(MessageConstant.UPLOAD_FAILED);
    }
}