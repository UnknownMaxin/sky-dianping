package com.maxin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maxin.constant.RedisConstant;
import com.maxin.entity.SeckillVoucher;
import com.maxin.entity.Voucher;
import com.maxin.mapper.VoucherMapper;
import com.maxin.service.SeckillVoucherService;
import com.maxin.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VoucherServiceImpl extends ServiceImpl<VoucherMapper, Voucher> implements VoucherService {

    @Autowired
    private SeckillVoucherService seckillVoucherService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 查询店铺的优惠券列表
     * @param shopId
     * @return
     */
    public List<Voucher> queryVoucherOfShop(Long shopId) {
        List<Voucher> vouchers = getBaseMapper().queryVoucherOfShop(shopId);
        return vouchers;
    }

    /**
     * 新增秒杀券
     * @param voucher
     */
    @Transactional
    public void addSeckillVoucher(Voucher voucher) {
        // 保存优惠券
        save(voucher);
        // 保存秒杀信息
        SeckillVoucher seckillVoucher = new SeckillVoucher();
        seckillVoucher.setVoucherId(voucher.getId());
        seckillVoucher.setStock(voucher.getStock());
        seckillVoucher.setBeginTime(voucher.getBeginTime());
        seckillVoucher.setEndTime(voucher.getEndTime());
        seckillVoucherService.save(seckillVoucher);
        // 保存秒杀库存到Redis中
        stringRedisTemplate.opsForValue().set(RedisConstant.SECKILL_STOCK_KEY + voucher.getId(), voucher.getStock().toString());
    }
}
