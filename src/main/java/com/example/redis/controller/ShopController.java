package com.example.redis.controller;

import com.example.redis.common.Result;
import com.example.redis.entity.po.Shop;
import com.example.redis.service.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/shop")
public class ShopController {

    @Autowired
    private ShopService shopService;

    // 創建商店
    @PostMapping("/create")
    public Result<String> createShop(@RequestBody Shop shop) {
        boolean saved = shopService.save(shop);
        return saved ? Result.success("商店創建成功") : Result.error("商店創建失敗");
    }

    // 獲取所有商店
    @GetMapping("/all")
    public Result<List<Shop>> getAllShops() {
        List<Shop> shops = shopService.list();
        return Result.success(shops);
    }

    // 根據 ID 查詢商店
    @GetMapping("/{id}")
    public Result<Shop> getShopById(@PathVariable Long id) {
        return shopService.queryById(id);
    }

    // 更新商店
    @PutMapping("/update")
    public Result<String> updateShop(@RequestBody Shop shop) {
        boolean updated = shopService.updateById(shop);
        return updated ? Result.success("商店更新成功") : Result.error("商店更新失敗");
    }

    // 刪除商店
    @DeleteMapping("/delete/{id}")
    public Result<String> deleteShop(@PathVariable Long id) {
        boolean removed = shopService.removeById(id);
        return removed ? Result.success("商店刪除成功") : Result.error("商店刪除失敗");
    }
}
