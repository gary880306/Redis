package com.example.redis.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.redis.common.Result;
import com.example.redis.entity.dto.UserDto;
import com.example.redis.entity.po.Blog;

import java.util.List;

public interface BlogService extends IService<Blog> {
    List<Blog> queryLatestBlogs(Integer limit);

    Blog queryBlogDetail(Long id);

    Result<String> likeBlog(Long id);

    Result<List<UserDto>> getBlogLikes(Long id);
}
