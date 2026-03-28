package com.maxin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.maxin.entity.Shop;

public interface ShopService extends IService<Shop> {

    /**
     * 根据id查询商铺
     * @param id
     * @return
     */
    Shop queryById(Long id);


    /**
     * 更新商铺信息
     * @param shop
     */
    void updateShop(Shop shop);
}
