package com.lowcarbon.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.lowcarbon.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Tag(name = "文件上传模块", description = "图片上传等接口")
@RestController
@RequestMapping("/file")
public class FileController {

    @Value("${server.port}")
    private String port;

    private static final String UPLOAD_PATH = "uploads/";

    @Operation(summary = "上传文件", description = "上传图片文件，返回访问URL")
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("上传文件不能为空");
        }

        // 获取原文件名
        String originalFilename = file.getOriginalFilename();
        // 获取文件后缀
        String suffix = FileUtil.extName(originalFilename);
        // 生成新文件名
        String fileName = IdUtil.fastSimpleUUID() + "." + suffix;

        // 创建文件对象
        File dest = new File(System.getProperty("user.dir") + File.separator + UPLOAD_PATH + fileName);
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }

        try {
            // 保存文件
            file.transferTo(dest);
            // 返回URL (假设本地运行)
            String url = "http://localhost:" + port + "/uploads/" + fileName;
            return Result.success(url);
        } catch (IOException e) {
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }
}