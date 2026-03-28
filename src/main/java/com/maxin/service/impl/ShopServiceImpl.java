package com.maxin.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maxin.constant.MessageConstant;
import com.maxin.constant.RedisConstant;
import com.maxin.entity.RedisData;
import com.maxin.entity.Shop;
import com.maxin.exception.ShopIsNotExistException;
import com.maxin.mapper.ShopMapper;
import com.maxin.service.ShopService;
import com.maxin.utils.CacheClientUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements ShopService {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private CacheClientUtils cacheClientUtils;

    /**
     * 根据id查询商铺
     * @param id
     * @return
     */
    public Shop queryById(Long id) {
        // 解决缓存穿透
        Shop shop = cacheClientUtils.queryWithPassThrough(
                RedisConstant.CACHE_SHOP_KEY, id, Shop.class, this::getById, RedisConstant.CACHE_SHOP_TTL, TimeUnit.SECONDS
        );

        if (shop == null) {
            throw new ShopIsNotExistException(MessageConstant.SHOP_IS_NOT_EXIST);
        }

        return shop;
    }

    /**
     * 更新商铺信息
     * @param shop
     */
    @Transactional
    public void updateShop(Shop shop) {
        Long id = shop.getId();
        if (id == null) {
            throw new ShopIsNotExistException(MessageConstant.SHOP_ID_IS_NOT_EMPTY);
        }
        updateById(shop);
        redisTemplate.delete(RedisConstant.CACHE_SHOP_KEY + id);
    }
}
