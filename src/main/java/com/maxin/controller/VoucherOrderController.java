package com.maxin.controller;


import com.maxin.result.Result;
import com.maxin.service.VoucherOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/voucher-order")
@Tag(name = "优惠券订单相关接口")
@Slf4j
public class VoucherOrderController {

    @Resource
    private VoucherOrderService voucherOrderService;

    /**
     * 优惠券秒杀
     * @param voucherId
     * @return
     */
    @PostMapping("/seckill/{id}")
    @Operation(summary = "优惠券秒杀")
    public Result<Long> seckillVoucher(@PathVariable("id") Long voucherId) {
        Long orderId = voucherOrderService.seckillVoucher(voucherId);
        return Result.success(orderId);
    }
}
