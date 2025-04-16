package com.example.redis.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.redis.common.Result;
import com.example.redis.entity.dto.UserDto;
import com.example.redis.entity.po.Blog;
import com.example.redis.entity.po.Shop;
import com.example.redis.entity.po.User;
import com.example.redis.mapper.BlogMapper;
import com.example.redis.service.BlogService;
import com.example.redis.service.ShopService;
import com.example.redis.service.UserService;
import com.example.redis.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Collections;

import static com.example.redis.utils.RedisConstants.BLOG_LIKED_KEY;

@Slf4j
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements BlogService {

    @Autowired
    private BlogMapper blogMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private ShopService shopService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public List<Blog> queryLatestBlogs(Integer limit) {
        log.info("查詢最新Blog, limit = {}", limit);

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

    @Override
    public Blog queryBlogDetail(Long id) {
        log.info("查詢部落格詳情, id = {}", id);
        try {
            // 1. 使用 getById 獲取基本資訊
            Blog blog = getById(id);
            if (blog == null) {
                return null;
            }

            // 2. 查詢並填充用戶資訊
            User user = userService.getById(blog.getUserId());
            if (user != null) {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("userId", user.getUserId());
                userInfo.put("username", user.getUsername());
                // 可以視需要添加其他用戶資訊
                blog.setUserInfo(userInfo);
            }

            // 3. 查詢並填充商店資訊
            if (blog.getShopId() != null) {
                Shop shop = shopService.getById(blog.getShopId());
                if (shop != null) {
                    Map<String, Object> shopInfo = new HashMap<>();
                    shopInfo.put("shopId", shop.getShopId());
                    shopInfo.put("name", shop.getName());
                    shopInfo.put("address", shop.getAddress());
                    shopInfo.put("imageUrl", shop.getImageUrl());
                    blog.setShopInfo(shopInfo);
                }
            }

            // 4. 檢查 Blog 是否點過讚
            isLiked(blog);

            return blog;
        } catch (Exception e) {
            log.error("查詢部落格詳情失敗", e);
            throw e;
        }
    }

    @Override
    public Result<String> likeBlog(Long id) {
        // 1.獲取登入用戶
         Long userId = UserHolder.getUser().getUserId();
        // 2. 判斷當前用戶是否已經點過讚
        String key = BLOG_LIKED_KEY + id;
        Double score = stringRedisTemplate.opsForZSet().score(key, userId.toString());
        if (score == null) {
            // 3. 未點讚
            // 3.1. 資料庫 + 1
            boolean isSuccess = update().setSql("liked = liked + 1").eq("id", id).update();
            // 3.2. 保存用戶到 redis sortedset 集合
            if (isSuccess) {
                stringRedisTemplate.opsForZSet().add(key, userId.toString(), System.currentTimeMillis());
            }
        } else {
            // 4. 已經點讚
            // 4.1. 資料庫 -1
            boolean isSuccess = update().setSql("liked = liked - 1").eq("id", id).update();
            // 4.2 把用戶從 redis set 集合中移除
            if (isSuccess) {
                stringRedisTemplate.opsForZSet().remove(key, userId.toString());
            }
        }
        return Result.success("成功");
    }

    @Override
    public Result<List<UserDto>> getBlogLikes(Long id) {
        String key = BLOG_LIKED_KEY + id;
        // 1. 查詢 top5 點讚用戶
        Set<String> top5 = stringRedisTemplate.opsForZSet().range(key, 0, 4);

        if (top5 == null || top5.isEmpty()) {
            return Result.success(Collections.emptyList());
        }

        // 2. 提取用戶ID
        List<Long> ids = top5.stream().map(Long::valueOf).toList();
        String idStr = StrUtil.join(",", ids);

        // 3. 根據用戶ID查找用戶
        List<UserDto> userDtoList = userService.query().in("user_id", ids).last("ORDER BY FIELD(user_id," + idStr + ")").list()
                .stream()
                .map(user -> BeanUtil.copyProperties(user, UserDto.class))
                .toList();

        // 4. 返回
        return Result.success(userDtoList);
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

    private void isLiked(Blog blog) {
        UserDto user = UserHolder.getUser();
        // 1.獲取登入用戶
         Long userId = UserHolder.getUser().getUserId();
        // 2. 判斷當前用戶是否已經點過讚
        String key = BLOG_LIKED_KEY + blog.getId();
        Double score = stringRedisTemplate.opsForZSet().score(key, userId.toString());
        blog.setIsLike(score != null);
    }
}