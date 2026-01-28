package com.lowcarbon.service;

import com.lowcarbon.entity.ExchangeRecord;
import com.lowcarbon.entity.VirtualProduct;

import java.util.List;

/**
 * 积分商城Service接口
 */
public interface ShopService {
    
    /**
     * 获取所有上架的虚拟商品列表
     */
    List<VirtualProduct> listProducts();
    
    /**
     * 兑换虚拟商品
     * @param userId 用户ID
     * @param productId 商品ID
     */
    void exchangeProduct(Long userId, Long productId);
    
    /**
     * 获取用户的兑换记录
     * @param userId 用户ID
     */
    List<ExchangeRecord> getUserExchangeRecords(Long userId);
    
    /**
     * 切换用户主题
     * @param userId 用户ID
     * @param themeStyle 主题样式
     */
    void changeTheme(Long userId, String themeStyle);
}
