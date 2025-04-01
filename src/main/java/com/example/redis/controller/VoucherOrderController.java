package com.example.redis.controller;

import com.example.redis.common.Result;
import com.example.redis.mapper.VoucherOrderMapper;
import com.example.redis.service.VoucherOrderService;
import com.example.redis.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 優惠券訂單控制器
 */
@RestController
@RequestMapping("/voucher-order")
public class VoucherOrderController {

    @Autowired
    private VoucherOrderService voucherOrderService;

    /**
     * 秒殺優惠券下單
     * @param voucherId 優惠券ID
     * @return 訂單ID
     */
    @PostMapping("/seckill/{id}")
    public ResponseEntity<?> seckillVoucher(@PathVariable("id") Long voucherId) {
        Result<Long> result = voucherOrderService.seckillVoucher(voucherId);

        if (result.getCode() != 200) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(result);
        }

        return ResponseEntity.ok(result.getData().toString());
    }
}