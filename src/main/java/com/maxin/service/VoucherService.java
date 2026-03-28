package com.maxin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.maxin.entity.Voucher;

import java.util.List;

public interface VoucherService extends IService<Voucher> {

    /**
     * 查询店铺的优惠券列表
     * @param shopId
     * @return
     */
    List<com.maxin.entity.Voucher> queryVoucherOfShop(Long shopId);

    /**
     * 新增秒杀券
     * @param voucher
     */
    void addSeckillVoucher(Voucher voucher);
}
