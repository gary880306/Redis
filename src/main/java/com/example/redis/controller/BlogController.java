package com.example.redis.controller;

import com.example.redis.common.Result;
import com.example.redis.entity.dto.UserDto;
import com.example.redis.entity.po.Blog;
import com.example.redis.service.BlogService;
import com.example.redis.service.UserService;
import com.example.redis.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/blog")
public class BlogController {

    @Autowired
    private BlogService blogService;

    @Autowired
    private UserService userService;

    @PostMapping
    public Result<Long> upload(@RequestBody Blog blog) throws IOException {
        // 獲取登入用戶
        UserDto user = UserHolder.getUser();
        blog.setUserId(user.getUserId());
        // 保存 Blog
        blogService.save(blog);

        // 返回
        return Result.success(blog.getId());
    }
}
