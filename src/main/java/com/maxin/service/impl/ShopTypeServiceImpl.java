package com.maxin.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maxin.constant.MessageConstant;
import com.maxin.constant.RedisConstant;
import com.maxin.entity.ShopType;
import com.maxin.exception.ShopTypeIsNotExistException;
import com.maxin.mapper.ShopTypeMapper;
import com.maxin.service.ShopTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements ShopTypeService {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 店铺类型查询
     * @return
     */
    public List<ShopType> queryTypeList() {
        List<ShopType> shopTypeList;

        String key = RedisConstant.CACHE_SHOP_TYPE_KEY;
        String json = redisTemplate.opsForValue().get(key).toString();
        if (StrUtil.isNotBlank(json)) {
            shopTypeList = JSONUtil.toList(json, ShopType.class);
            return shopTypeList;
        }

        shopTypeList = this.list(new LambdaQueryWrapper<ShopType>().orderByAsc(ShopType::getSort));
        if (shopTypeList == null || shopTypeList.size() == 0) {
            throw new ShopTypeIsNotExistException(MessageConstant.SHOP_TYPE_IS_NOT_EXIST);
        }

        redisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shopTypeList), RedisConstant.CACHE_SHOP_TYPE_TTL, TimeUnit.MINUTES);

        return shopTypeList;
    }
}
