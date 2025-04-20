package com.example.redis.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.redis.common.Result;
import com.example.redis.entity.dto.ScrollResult;
import com.example.redis.entity.dto.UserDto;
import com.example.redis.entity.po.Blog;
import com.example.redis.entity.po.Follow;
import com.example.redis.entity.po.Shop;
import com.example.redis.entity.po.User;
import com.example.redis.mapper.BlogMapper;
import com.example.redis.service.BlogService;
import com.example.redis.service.FollowService;
import com.example.redis.service.ShopService;
import com.example.redis.service.UserService;
import com.example.redis.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Collections;

import static com.example.redis.utils.RedisConstants.*;

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
    private FollowService followService;

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
                userInfo.put("avatar", user.getAvatar());
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

    @Override
    public Result<Long> saveBlog(Blog blog) {
        // 1. 獲取登入用戶
        UserDto user = UserHolder.getUser();
        blog.setUserId(user.getUserId());

        // 2. 保存 Blog
        boolean isSuccess = save(blog);
        if (!isSuccess) {
            return Result.error("新增Blog失敗!");
        }

        // 3. 取得作者的所有粉絲 select * from follow where follow_user_id = ?
        List<Follow> follows = followService.query().eq("follow_user_id", user.getUserId()).list();

        // 4. 推送Blog給所有粉絲
        for (Follow follow : follows) {
            // 4.1 取得粉絲ID
            Long userId = follow.getUserId();
            // 4.2 推送
            String key = FEED_KEY + userId;
            stringRedisTemplate.opsForZSet().add(key, blog.getId().toString(), System.currentTimeMillis());
        }

        // 5. 返回 ID
        return Result.success(blog.getId());
    }

    @Override
    public Result<ScrollResult> getBlogOfFollow(Long max, Integer offset) {
        // 1. 獲取登入用戶
        Long userId = UserHolder.getUser().getUserId();

        // 2. 查詢收件箱 ZREVRANGEBYSCORE key Max Min LIMIT offset count
        String key = FEED_KEY + userId;
        // TODO: 測試分頁limit先用1，之後可照需求更改
        int limit = 1;
        Set<ZSetOperations.TypedTuple<String>> typedTuples = stringRedisTemplate.opsForZSet().reverseRangeByScoreWithScores(key, 0, max, offset, limit);
        if (typedTuples == null || typedTuples.isEmpty()) {
            return Result.success();
        }

        // 3. 分析數據: blogId, minTime(時間戳), offset(偏移量)
        List<Long> ids = new ArrayList<>(typedTuples.size());
        long minTime = 0;
        int os = 1;
        for (ZSetOperations.TypedTuple<String> typedTuple : typedTuples) { // ex: 5, 4, 4, 2, 2
            // 3.1 獲取blogId
            ids.add(Long.valueOf(typedTuple.getValue()));
            // 3.2 獲取分數(時間戳)
            long time = typedTuple.getScore().longValue();
            if (time == minTime) {
                os ++;
            } else {
                minTime = time;
                os = 1;
            }
        }

        // 4. 利用blogId查詢blog
        String idStr = StrUtil.join(",", ids);
        List<Blog> blogList = query().in("id", ids).last("ORDER BY FIELD(id," + idStr + ")").list();

        // 4. 封裝並返回
        ScrollResult result = new ScrollResult();
        result.setList(blogList);
        result.setOffset(os);
        result.setMinTime(minTime);
        boolean hasNext = typedTuples.size() == limit;
        result.setHasNext(hasNext);
        return Result.success(result);
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