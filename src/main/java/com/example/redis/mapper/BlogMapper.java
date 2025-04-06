package com.example.redis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.redis.entity.po.Blog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface BlogMapper extends BaseMapper<Blog> {
    /**
     * 查詢最新部落格，同時獲取用戶和商店資訊
     * @param limit 限制返回的數量
     * @return 包含用戶和商店資訊的部落格列表
     */
    @Select("SELECT b.*, " +
            "u.user_id, u.username, " +
            "s.shop_id, s.name as shop_name, s.address as shop_address, s.image_url " +
            "FROM blog b " +
            "LEFT JOIN user u ON b.user_id = u.user_id " +
            "LEFT JOIN shop s ON b.shop_id = s.shop_id " +
            "ORDER BY b.create_time DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> selectLatestBlogsWithUserAndShop(@Param("limit") Integer limit);
}