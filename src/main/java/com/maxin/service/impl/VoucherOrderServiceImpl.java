package com.maxin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maxin.constant.MessageConstant;
import com.maxin.entity.VoucherOrder;
import com.maxin.exception.SeckillVoucherException;
import com.maxin.mapper.VoucherOrderMapper;
import com.maxin.service.SeckillVoucherService;
import com.maxin.service.VoucherOrderService;
import com.maxin.utils.RedisIdWorker;
import com.maxin.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Collections;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements VoucherOrderService {

    @Resource
    private SeckillVoucherService seckillVoucherService;
    @Resource
    private RedisIdWorker redisIdWorker;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private RedissonClient redissonClient;

    private static final DefaultRedisScript<Long> SECKILL_VOUCHER_SCRIPT;
    static {
        SECKILL_VOUCHER_SCRIPT = new DefaultRedisScript<>();
        SECKILL_VOUCHER_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
        SECKILL_VOUCHER_SCRIPT.setResultType(Long.class);
    }

    private BlockingQueue<VoucherOrder> orderTasks = new ArrayBlockingQueue<VoucherOrder>(1024 * 1024);
    private static final ExecutorService SECKILL_ORDER_EXECUTOR = Executors.newSingleThreadExecutor();

    @PostConstruct
    private void init() {
        SECKILL_ORDER_EXECUTOR.submit(new VoucherOrderHandler());
    }

    private class VoucherOrderHandler implements Runnable {
        public void run() {
            while (true) {
                try {
                    VoucherOrder voucherOrder = orderTasks.take();
                    handleVoucherHandler(voucherOrder);
                } catch (Exception e) {
                    throw new SeckillVoucherException(MessageConstant.VOUCHER_ORDER_HANDLER_ERROR);
                }
            }
        }
    }

    private VoucherOrderService proxy; // 由于获取代理对象也需要走线程，所以只能在外面定义好传进去了

    private void handleVoucherHandler(VoucherOrder voucherOrder) {
        // 由于多线程，获取用户仅可从voucherOrder中获取了
        Long userId = voucherOrder.getUserId();
        // SimpleRedisLock lock = new SimpleRedisLock("order:" + userId, stringRedisTemplate);
        RLock lock = redissonClient.getLock("order:" + userId);
        boolean isLock = lock.tryLock();
        if (!isLock) {
            throw new SeckillVoucherException(MessageConstant.MAXIMUM_PURCHASE_TIMES);
        }
        // 获取代理对象（事务）
        try {
            proxy.createVoucherOrder(voucherOrder);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 优惠券秒杀
     * @param voucherId
     * @return
     */
    public Long seckillVoucher(Long voucherId) {
        Long userId = UserHolder.getUser().getId();

        Long result = stringRedisTemplate.execute(SECKILL_VOUCHER_SCRIPT, Collections.emptyList(), voucherId.toString(), userId.toString());
        int r = result.intValue();
        if (r != 0) {
            String msg = r == 1 ? MessageConstant.INSUFFICIENT_VOUCHERS : MessageConstant.MAXIMUM_PURCHASE_TIMES;
            throw new SeckillVoucherException(msg);
        }

        Long orderId = redisIdWorker.nextId("order");
        VoucherOrder voucherOrder = new VoucherOrder();
        voucherOrder.setId(orderId);
        voucherOrder.setUserId(userId);
        voucherOrder.setVoucherId(voucherId);

        // 保存阻塞队列
        orderTasks.add(voucherOrder);
        // 获取代理对象（事务）
        proxy = (VoucherOrderService) AopContext.currentProxy();

        return orderId;
    }

    /**
     * 创建优惠券秒杀订单
     * @param voucherOrder
     */
    @Transactional
    public void createVoucherOrder(VoucherOrder voucherOrder) {
        // 用户下单限制，一人一单
        Long userId = UserHolder.getUser().getId();

        Long count = query().eq("user_id", userId)
                .eq("voucher_id", voucherOrder.getVoucherId())
                .count();
        if (count > 0) {
            throw new SeckillVoucherException(MessageConstant.MAXIMUM_PURCHASE_TIMES);
        }

        // 扣减库存
        boolean update = seckillVoucherService.update()
                .setSql("stock = stock - 1")
                .eq("voucher_id", voucherOrder.getVoucherId())
                // .eq("stock", voucher.getStock())  // 乐观锁
                .gt("stock", 0)
                .update();
        if (!update) {
            throw new SeckillVoucherException(MessageConstant.MAXIMUM_PURCHASE_TIMES);
        }

        save(voucherOrder);
    }
}
