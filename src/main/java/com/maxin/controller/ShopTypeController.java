package com.maxin.controller;


import com.maxin.entity.ShopType;
import com.maxin.result.Result;
import com.maxin.service.ShopTypeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/shop-type")
@Api(tags = "店铺类型相关接口")
@Slf4j
public class ShopTypeController {
    @Resource
    private ShopTypeService typeService;

    /**
     * 店铺类型查询
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("店铺类型查询")
    public Result<List<ShopType>> queryTypeList() {
        List<ShopType> shopTypeList = typeService.queryTypeList();
        return Result.success(shopTypeList);
    }
}
