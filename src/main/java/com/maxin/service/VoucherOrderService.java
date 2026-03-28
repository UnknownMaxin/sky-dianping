package com.maxin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.maxin.entity.VoucherOrder;

public interface VoucherOrderService extends IService<VoucherOrder> {

    /**
     * 优惠券秒杀
     *
     * @param voucherId
     * @return
     */
    Long seckillVoucher(Long voucherId);

    /**
     * 创建优惠券秒杀订单
     *
     * @param voucherOrder
     */
    void createVoucherOrder(VoucherOrder voucherOrder);
}
