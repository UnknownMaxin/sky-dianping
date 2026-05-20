package com.maxin.utils;

import com.maxin.entity.Blog;
import com.maxin.entity.Shop;
import com.maxin.entity.ShopType;
import com.maxin.entity.Voucher;
import com.maxin.service.BlogService;
import com.maxin.service.ShopService;
import com.maxin.service.ShopTypeService;
import com.maxin.service.VoucherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class AiChatUtils {

    @Autowired
    private ShopService shopService;

    @Autowired
    private VoucherService voucherService;

    @Autowired
    private BlogService blogService;

    @Autowired
    private ShopTypeService shopTypeService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Tool(description = "根据店铺ID查询店铺")
    public Shop queryShopById(Long id) {
        log.info("AI调用工具查询店铺，ID: {}", id);
        return shopService.queryById(id);
    }

    @Tool(description = "根据店铺名称模糊查询店铺")
    public List<Shop> queryShopByName(String name) {
        log.info("AI调用工具查询店铺，名称: {}", name);
        return shopService.lambdaQuery().like(Shop::getName, name).list();
    }

    @Tool(description = "根据店铺类型ID查询店铺")
    public List<Shop> queryShopByType(Long typeId) {
        log.info("AI调用工具查询店铺，类型ID: {}", typeId);
        return shopService.lambdaQuery().eq(Shop::getTypeId, typeId).list();
    }

    @Tool(description = "根据商圈查询店铺")
    public List<Shop> queryShopByArea(String area) {
        log.info("AI调用工具查询店铺，商圈: {}", area);
        return shopService.lambdaQuery().like(Shop::getArea, area).list();
    }

    @Tool(description = "查询所有店铺")
    public List<Shop> queryAllShops() {
        log.info("AI调用工具查询所有店铺");
        return shopService.list();
    }

    @Tool(description = "查询所有店铺类型")
    public Boolean queryUserLikedBlog(Long userId, Long blogId) {
        log.info("AI调用工具查询用户是否点赞笔记，用户ID: {}, 笔记ID: {}", userId, blogId);
        String key = "blog:liked:" + blogId;
        Double score = redisTemplate.opsForZSet().score(key, userId.toString());
        return score != null;
    }

    @Tool(description = "查询店铺的优惠券和秒杀券列表")
    public List<Voucher> queryVouchersByShop(Long shopId) {
        log.info("AI调用工具查询店铺优惠券，店铺ID: {}", shopId);
        return voucherService.queryVoucherOfShop(shopId);
    }

    @Tool(description = "根据用户喜好推荐店铺")
    public List<Shop> queryShopRecommendations(Long userId, Integer limit) {
        log.info("AI调用工具推荐店铺，用户ID: {}, 数量: {}", userId, limit);
        
        List<Long> likedShopTypes = getUserLikedShopTypes(userId);
        
        if (likedShopTypes.isEmpty()) {
            return shopService.lambdaQuery()
                    .orderByDesc(Shop::getScore)
                    .last("LIMIT " + limit)
                    .list();
        }
        
        return shopService.lambdaQuery()
                .in(Shop::getTypeId, likedShopTypes)
                .orderByDesc(Shop::getScore)
                .last("LIMIT " + limit)
                .list();
    }

    @Tool(description = "查询所有店铺类型")
    public List<ShopType> queryAllShopTypes() {
        log.info("AI调用工具查询所有店铺类型");
        return shopTypeService.list();
    }

    @Tool(description = "获取热门笔记列表")
    public List<Blog> getHotBlogs(Integer limit) {
        log.info("AI调用工具获取热门笔记，数量: {}", limit);
        List<Blog> blogs = blogService.lambdaQuery()
                .orderByDesc(Blog::getLiked)
                .last("LIMIT " + limit)
                .list();
        return blogs;
    }

    private List<Long> getUserLikedShopTypes(Long userId) {
        List<Long> likedBlogIds = getLikedBlogIds(userId);
        
        List<Long> shopTypeIds = new ArrayList<>();
        for (Long blogId : likedBlogIds) {
            try {
                Blog blog = blogService.getById(blogId);
                if (blog != null && blog.getShopId() != null) {
                    Shop shop = shopService.getById(blog.getShopId());
                    if (shop != null && shop.getTypeId() != null) {
                        shopTypeIds.add(shop.getTypeId());
                    }
                }
            } catch (Exception e) {
                log.warn("获取用户喜好店铺类型失败", e);
            }
        }
        
        return shopTypeIds.stream().distinct().toList();
    }

    private List<Long> getLikedBlogIds(Long userId) {
        List<Long> likedBlogIds = new ArrayList<>();
        try {
            List<Blog> allBlogs = blogService.list();
            for (Blog blog : allBlogs) {
                String key = "blog:liked:" + blog.getId();
                Double score = redisTemplate.opsForZSet().score(key, userId.toString());
                if (score != null) {
                    likedBlogIds.add(blog.getId());
                }
            }
        } catch (Exception e) {
            log.warn("获取用户点赞笔记失败", e);
        }
        return likedBlogIds;
    }
}
