package com.example.redis.controller;

import com.example.redis.common.Result;
import com.example.redis.entity.dto.ScrollResult;
import com.example.redis.entity.dto.UserDto;
import com.example.redis.entity.po.Blog;
import com.example.redis.service.BlogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/blog")
public class BlogController {

    @Autowired
    private BlogService blogService;

    /**
     * 發布Blog文章
     *
     * @param blog Blog文章詳情
     * @return Long BlogId
     */
    @PostMapping
    public Result<Long> upload(@RequestBody Blog blog) throws IOException {
        return blogService.saveBlog(blog);
    }

    /**
     * 獲取最新Blog
     *
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
     *
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
     *
     * @param id blogId
     * @return String 儲存結果
     */
    @PutMapping("/like/{id}")
    public Result<String> likeBlog(@PathVariable("id") Long id) {
        return blogService.likeBlog(id);
    }

    /**
     * Blog點讚排行榜
     *
     * @param id blogId
     * @return List<UserDto> 用戶資訊列表
     */
    @GetMapping("/likes/{id}")
    public Result<List<UserDto>> getBlogLikes(@PathVariable("id") Long id) {
        return blogService.getBlogLikes(id);
    }


    /**
     * 獲取個人頁面關注文章列表
     *
     * @param max    查詢最大時間
     * @param offset 偏移量
     * @return List<Blog> Blog文章列表
     */
    @GetMapping("/of/follow")
    public Result<ScrollResult> getBlogOfFollow(@RequestParam("lastId") Long max, @RequestParam(value = "offset", defaultValue = "0") Integer offset) {
        return blogService.getBlogOfFollow(max, offset);
    }
}
