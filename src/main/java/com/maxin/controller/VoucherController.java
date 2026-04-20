package com.maxin.controller;


import com.maxin.result.Result;
import com.maxin.entity.Voucher;
import com.maxin.service.VoucherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/voucher")
@Tag(name = "优惠券相关接口")
@Slf4j
public class VoucherController {

    @Resource
    private VoucherService voucherService;

    /**
     * 新增普通券
     * @param voucher 优惠券信息
     * @return 优惠券id
     */
    @PostMapping
    @Operation(summary = "新增普通券")
    public Result<Long> addVoucher(@RequestBody Voucher voucher) {
        voucherService.save(voucher);
        return Result.success(voucher.getId());
    }

    /**
     * 新增秒杀券
     * @param voucher 优惠券信息，包含秒杀信息
     * @return 优惠券id
     */
    @PostMapping("/seckill")
    @Operation(summary = "新增秒杀券")
    public Result<Long> addSeckillVoucher(@RequestBody Voucher voucher) {
        voucherService.addSeckillVoucher(voucher);
        return Result.success(voucher.getId());
    }

    /**
     * 查询店铺的优惠券列表
     * @param shopId 店铺id
     * @return 优惠券列表
     */
    @GetMapping("/list/{shopId}")
    @Operation(summary = "查询优惠券列表")
    public Result<List<Voucher>> queryVoucherOfShop(@PathVariable("shopId") Long shopId) {
        List<Voucher> voucherList = voucherService.queryVoucherOfShop(shopId);
        return Result.success(voucherList);
    }
}
