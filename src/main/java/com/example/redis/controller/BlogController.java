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
import java.util.List;

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
        // TODO:TEST USER
//        UserDto user = UserHolder.getUser();
//        blog.setUserId(user.getUserId());
        blog.setUserId(1L);
        // 保存 Blog
        blogService.save(blog);

        // 返回
        return Result.success(blog.getId());
    }

    /**
     * 獲取最新部落格
     * @param limit 限制返回的數量，默認為 4 篇
     * @return 部落格列表
     */
    @GetMapping("/latest")
    public Result<List<Blog>> getLatestBlogs(@RequestParam(defaultValue = "4") Integer limit) {
        log.info("獲取最新部落格, limit = {}", limit);
        try {
            // 查詢最新的部落格
            List<Blog> blogs = blogService.queryLatestBlogs(limit);
            return Result.success(blogs);
        } catch (Exception e) {
            log.error("獲取最新部落格失敗", e);
            return Result.error("獲取最新部落格失敗: " + e.getMessage());
        }
    }

}
