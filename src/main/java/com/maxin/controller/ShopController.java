package com.maxin.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.maxin.constant.SystemConstant;
import com.maxin.entity.Shop;
import com.maxin.result.Result;
import com.maxin.service.ShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/shop")
@Tag(name = "商店相关接口")
@Slf4j
public class ShopController {

    @Autowired
    private ShopService shopService;

    /**
     * 根据id查询商铺信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据id查询商铺信息")
    public Result<Shop> queryShopById(@PathVariable("id") Long id) {
        Shop shop = shopService.queryById(id);
        return Result.success(shop);
    }

    /**
     * 新增商铺信息
     * @param shop
     * @return
     */
    @PostMapping
    @Operation(summary = "新增商铺信息")
    public Result<Long> saveShop(@RequestBody Shop shop) {
        shopService.save(shop);
        return Result.success(shop.getId());
    }

    /**
     * 更新店铺信息
     * @param shop 店铺数据
     * @return 无
     */
    @PutMapping
    @Operation(summary = "更新店铺信息")
    public Result updateShop(@RequestBody Shop shop) {
        shopService.updateShop(shop);
        return Result.success();
    }

    /**
     * 根据店铺类型分页查询店铺信息
     * @param typeId 店铺类型
     * @param current 页码
     * @return 店铺列表
     */
    @GetMapping("/of/type")
    @Operation(summary = "根据店铺类型分页查询店铺信息")
    public Result<List<Shop>> queryShopByType(
            @RequestParam("typeId") Integer typeId,
            @RequestParam(value = "current", defaultValue = "1") Integer current
    ) {
        // 根据类型分页查询
        Page<Shop> page = shopService.query()
                .eq("type_id", typeId)
                .page(new Page<>(current, SystemConstant.DEFAULT_PAGE_SIZE));
        // 返回数据
        return Result.success(page.getRecords());
    }

    /**
     * 根据店铺名称关键字分页查询店铺信息
     * @param name 店铺名称关键字
     * @param current 页码
     * @return 店铺列表
     */
    @GetMapping("/of/name")
    @Operation(summary = "根据店铺名称关键字分页查询店铺信息")
    public Result<List<Shop>> queryShopByName(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "current", defaultValue = "1") Integer current
    ) {
        // 根据类型分页查询
        Page<Shop> page = shopService.query()
                .like(StrUtil.isNotBlank(name), "name", name)
                .page(new Page<>(current, SystemConstant.MAX_PAGE_SIZE));
        // 返回数据
        return Result.success(page.getRecords());
    }


}
