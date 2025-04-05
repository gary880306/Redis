package com.example.redis.controller;

import com.example.redis.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

import static com.example.redis.utils.SystemConstants.IMAGE_UPLOAD_DIR;

@Slf4j
@RestController
@RequestMapping("/upload")
public class UploadController {

    @PostMapping
    public Result<String> upload(@RequestParam("file") MultipartFile file) throws IOException {
        try {
            // 獲取原始文件名稱
            String originalFilename = file.getOriginalFilename();

            // 保存文件
            file.transferTo(new File(IMAGE_UPLOAD_DIR, originalFilename));

            log.debug("文件上傳成功, {}", originalFilename);
            return Result.success(originalFilename);
        } catch (IOException e) {
            throw new IOException("文件上傳失敗", e);
        }
    }

}
