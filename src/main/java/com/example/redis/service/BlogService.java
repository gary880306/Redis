package com.example.redis.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.redis.entity.po.Blog;

import java.util.List;

public interface BlogService extends IService<Blog> {
    List<Blog> queryLatestBlogs(Integer limit);
}
