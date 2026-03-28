package com.maxin.controller;


import com.maxin.result.Result;
import com.maxin.service.VoucherOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/voucher-order")
@Api(tags = "优惠券订单相关接口")
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
    @ApiOperation("优惠券秒杀")
    public Result<Long> seckillVoucher(@PathVariable("id") Long voucherId) {
        Long orderId = voucherOrderService.seckillVoucher(voucherId);
        return Result.success(orderId);
    }
}
