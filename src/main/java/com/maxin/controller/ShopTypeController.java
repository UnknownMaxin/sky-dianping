package com.maxin.controller;


import com.maxin.entity.ShopType;
import com.maxin.result.Result;
import com.maxin.service.ShopTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/shop-type")
@Tag(name = "店铺类型相关接口")
@Slf4j
public class ShopTypeController {

    @Autowired
    private ShopTypeService typeService;

    /**
     * 店铺类型查询
     * @return
     */
    @GetMapping("/list")
    @Operation(summary = "店铺类型查询")
    public Result<List<ShopType>> queryTypeList() {
        List<ShopType> shopTypeList = typeService.queryTypeList();
        return Result.success(shopTypeList);
    }
}
