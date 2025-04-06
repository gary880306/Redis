package com.example.redis.controller;

import cn.hutool.core.lang.UUID;
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

            // 檢查原始檔名並安全地獲取副檔名
            if (originalFilename == null || originalFilename.isEmpty()) {
                log.debug("originalFilename: {}, length: {}", originalFilename,
                        originalFilename != null ? originalFilename.length() : "null");
                return Result.error("檔案名稱不能為空");
            }

            String extension = "";
            if (originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            // 生成唯一檔名
            String newFileName = UUID.randomUUID().toString() + extension;

            // 確保上傳目錄存在
            File uploadDir = new File(IMAGE_UPLOAD_DIR);
            if (!uploadDir.exists()) {
                boolean created = uploadDir.mkdirs();
                if (!created) {
                    log.error("無法創建上傳目錄: {}", IMAGE_UPLOAD_DIR);
                    return Result.error("無法創建上傳目錄");
                }
            }

            // 保存文件（使用新檔名）
            File destFile = new File(uploadDir, newFileName);
            file.transferTo(destFile);

            log.debug("文件上傳成功, 檔名: {}", newFileName);
            return Result.success(newFileName);
        } catch (IOException e) {
            log.error("文件上傳失敗", e);
            throw new IOException("文件上傳失敗", e);
        }
    }

}
