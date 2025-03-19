package com.example.redis.controller;

import com.example.redis.common.Result;
import com.example.redis.entity.dto.VoucherDto;
import com.example.redis.entity.po.Voucher;
import com.example.redis.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/voucher")
public class VoucherController {

    @Autowired
    private VoucherService voucherService;

    /**
     * 查詢商店的優惠券列表
     */
    @GetMapping("/list/{shopId}")
    public Result<List<Voucher>> queryVoucherOfShop(@PathVariable("shopId") Long shopId) {
        List<Voucher> vouchers = voucherService.queryVoucherOfShop(shopId);
        return Result.success(vouchers);
    }
}
