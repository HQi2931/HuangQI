package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/admin/common")
public class UploadController {
    @Autowired
    private AliOssUtil aliOssUtil; // 阿里云OSS操作工具类
    @PostMapping("/upload")
    public Result upload(MultipartFile file) throws Exception {
        log.info("文件上传{}", file.getOriginalFilename());
        //文件交给OSS进行存储
        String url = aliOssUtil.upload(file.getBytes(), file.getOriginalFilename());
        log.info("文件上传完成，文件访问地址{}", url);
        return Result.success(url);
    }
}
