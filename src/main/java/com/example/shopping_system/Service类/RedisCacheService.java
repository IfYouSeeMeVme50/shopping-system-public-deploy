package com.example.shopping_system.Service类;

import com.example.shopping_system.实体类.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Redis缓存服务
 * 用于缓存热点商品数据，减轻数据库查询压力
 */
@Service
public class RedisCacheService {

    private static final Logger log = LoggerFactory.getLogger(RedisCacheService.class);

    private static final String PRODUCT_CACHE_KEY = "product:cache:";
    private static final String PRODUCT_LIST_CACHE_KEY = "product:list:all";
    private static final long CACHE_TTL_MINUTES = 10;

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisCacheService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 缓存单个商品（Redis不可用时静默降级，不影响主流程）
     */
    public void cacheProduct(Product product) {
        try {
            String key = PRODUCT_CACHE_KEY + product.getId();
            redisTemplate.opsForValue().set(key, product, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
            log.debug("商品已缓存: id={}", product.getId());
        } catch (Exception e) {
            log.warn("Redis缓存写入失败（已降级为直查数据库）: id={}, 原因: {}", product.getId(), e.getMessage());
        }
    }

    /**
     * 获取缓存的单个商品（Redis不可用时返回null，走数据库查询）
     */
    public Product getCachedProduct(Long productId) {
        try {
            String key = PRODUCT_CACHE_KEY + productId;
            Object value = redisTemplate.opsForValue().get(key);
            if (value instanceof Product) {
                log.debug("从Redis缓存获取商品: id={}", productId);
                return (Product) value;
            }
            return null;
        } catch (Exception e) {
            log.warn("Redis缓存读取失败（已降级为直查数据库）: id={}, 原因: {}", productId, e.getMessage());
            return null;
        }
    }

    /**
     * 缓存商品列表（首页展示商品）
     */
    public void cacheProductList(List<Product> products) {
        try {
            redisTemplate.opsForValue().set(PRODUCT_LIST_CACHE_KEY, products, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
            log.debug("商品列表已缓存，共 {} 条", products.size());
        } catch (Exception e) {
            log.warn("Redis列表缓存写入失败（已降级为直查数据库）: 原因: {}", e.getMessage());
        }
    }

    /**
     * 获取缓存的商品列表（Redis不可用时返回null，走数据库查询）
     */
    @SuppressWarnings("unchecked")
    public List<Product> getCachedProductList() {
        try {
            Object value = redisTemplate.opsForValue().get(PRODUCT_LIST_CACHE_KEY);
            if (value instanceof List) {
                log.debug("从Redis缓存获取商品列表");
                return (List<Product>) value;
            }
            return null;
        } catch (Exception e) {
            log.warn("Redis列表缓存读取失败（已降级为直查数据库）: 原因: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 清除商品缓存（商品信息变更时调用；Redis不可用时跳过，不影响业务写入）
     */
    public void clearProductCache(Long productId) {
        try {
            String key = PRODUCT_CACHE_KEY + productId;
            redisTemplate.delete(key);
            // 同时清除列表缓存，保证数据一致性
            redisTemplate.delete(PRODUCT_LIST_CACHE_KEY);
            log.debug("商品缓存已清除: id={}", productId);
        } catch (Exception e) {
            log.warn("Redis缓存清除失败（缓存过期后自动一致）: id={}, 原因: {}", productId, e.getMessage());
        }
    }

    /**
     * 清除所有商品缓存
     */
    public void clearAllProductCache() {
        try {
            redisTemplate.delete(PRODUCT_LIST_CACHE_KEY);
            log.debug("所有商品列表缓存已清除");
        } catch (Exception e) {
            log.warn("Redis缓存清除失败（缓存过期后自动一致）: 原因: {}", e.getMessage());
        }
    }
}