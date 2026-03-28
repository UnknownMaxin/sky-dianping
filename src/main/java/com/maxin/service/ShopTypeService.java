package com.maxin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.maxin.entity.ShopType;

import java.util.List;

public interface ShopTypeService extends IService<ShopType> {

    /**
     * 店铺类型查询
     * @return
     */
    List<ShopType> queryTypeList();
}
