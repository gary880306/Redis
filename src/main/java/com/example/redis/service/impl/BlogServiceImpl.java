package com.example.redis.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.redis.entity.po.Blog;
import com.example.redis.mapper.BlogMapper;
import com.example.redis.service.BlogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements BlogService {

    @Autowired
    private BlogMapper blogMapper;

    @Override
    public List<Blog> queryLatestBlogs(Integer limit) {
        log.info("查詢最新部落格, limit = {}", limit);

        try {
            // 使用SQL JOIN直接獲取關聯數據
            List<Map<String, Object>> resultMaps = blogMapper.selectLatestBlogsWithUserAndShop(limit);

            // 將 Map 轉換為 Blog 對象
            List<Blog> blogs = new ArrayList<>(resultMaps.size());

            for (Map<String, Object> resultMap : resultMaps) {
                Blog blog = new Blog();

                // 設置部落格資訊
                blog.setId(getLongValue(resultMap, "id"));
                blog.setUserId(getLongValue(resultMap, "user_id"));
                blog.setShopId(getLongValue(resultMap, "shop_id"));
                blog.setTitle((String) resultMap.get("title"));
                blog.setContent((String) resultMap.get("content"));
                blog.setImages((String) resultMap.get("images"));
                blog.setLiked(getIntValue(resultMap, "liked"));
                blog.setComments(getIntValue(resultMap, "comments"));
                blog.setCreateTime((Timestamp) resultMap.get("create_time"));
                blog.setUpdateTime((Timestamp) resultMap.get("update_time"));

                // 設置用戶資訊
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("userId", getLongValue(resultMap, "user_id"));
                userInfo.put("username", resultMap.get("username"));
                blog.setUserInfo(userInfo);

                // 設置商店資訊
                Map<String, Object> shopInfo = new HashMap<>();
                shopInfo.put("shopId", getLongValue(resultMap, "shop_id"));
                shopInfo.put("name", resultMap.get("shop_name"));
                shopInfo.put("address", resultMap.get("shop_address"));
                shopInfo.put("imageUrl", resultMap.get("image_url"));
                blog.setShopInfo(shopInfo);

                blogs.add(blog);
            }

            return blogs;
        } catch (Exception e) {
            log.error("查詢最新部落格失敗", e);
            throw e;
        }
    }

    // 輔助方法：安全地獲取Long值
    private Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.valueOf(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // 輔助方法：安全地獲取Integer值
    private Integer getIntValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.valueOf(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}