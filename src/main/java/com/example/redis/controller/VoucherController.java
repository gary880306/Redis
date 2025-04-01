package com.example.redis.controller;

import com.example.redis.common.Result;
import com.example.redis.entity.dto.VoucherDto;
import com.example.redis.entity.po.Voucher;
import com.example.redis.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

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

    /**
     * 新增秒殺優惠券
     * @param  voucherDto 秒殺優惠券資訊
     * @return 秒殺優惠券id
     */
    @PostMapping("seckill")
    public Result<Long> addSeckillVoucher(@RequestBody VoucherDto voucherDto) {
        Long voucherId = voucherService.addSeckillVoucher(voucherDto);
        return Result.success(voucherId);
    }
}
