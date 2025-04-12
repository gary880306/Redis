package com.example.redis.controller;

import com.example.redis.common.Result;
import com.example.redis.entity.dto.UserDto;
import com.example.redis.entity.po.Blog;
import com.example.redis.service.BlogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/blog")
public class BlogController {

    @Autowired
    private BlogService blogService;

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
     * 獲取最新Blog
     * @param limit 限制返回的數量，默認為 4 篇
     * @return Blog列表
     */
    @GetMapping("/latest")
    public Result<List<Blog>> getLatestBlogs(@RequestParam(defaultValue = "4") Integer limit) {
        log.info("獲取最新Blog, limit = {}", limit);
        try {
            // 查詢最新的Blog
            List<Blog> blogs = blogService.queryLatestBlogs(limit);
            return Result.success(blogs);
        } catch (Exception e) {
            log.error("獲取最新Blog失敗", e);
            return Result.error("獲取最新Blog失敗: " + e.getMessage());
        }
    }

    /**
     * 根據ID獲取Blog詳情
     * @param id blogId
     * @return Blog詳情
     */
    @GetMapping("/{id}")
    public Result<Blog> getBlogById(@PathVariable("id") Long id) {
        log.info("獲取Blog詳情, id = {}", id);
        try {
            Blog blog = blogService.queryBlogDetail(id);
            if (blog == null) {
                return Result.error("Blog不存在");
            }
            return Result.success(blog);
        } catch (Exception e) {
            log.error("獲取Blog詳情失敗", e);
            return Result.error("獲取Blog詳情失敗: " + e.getMessage());
        }
    }

    /**
     * Blog點讚
     * @param id blogId
     * @return Blog詳情
     */
    @PutMapping("/like/{id}")
    public Result<String> likeBlog(@PathVariable("id") Long id) {
        return blogService.likeBlog(id);
    }

    /**
     * Blog點讚排行榜
     * @param id blogId
     * @return Blog詳情
     */
    @GetMapping("/likes/{id}")
    public Result<List<UserDto>> getBlogLikes(@PathVariable("id") Long id) {
        return blogService.getBlogLikes(id);
    }

}
