package com.lowcarbon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.lowcarbon.entity.ExchangeRecord;
import com.lowcarbon.entity.User;
import com.lowcarbon.entity.VirtualProduct;
import com.lowcarbon.mapper.ExchangeRecordMapper;
import com.lowcarbon.mapper.UserMapper;
import com.lowcarbon.mapper.VirtualProductMapper;
import com.lowcarbon.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 积分商城Service实现类
 */
@Service
@RequiredArgsConstructor
public class ShopServiceImpl implements ShopService {
    
    private final VirtualProductMapper virtualProductMapper;
    private final ExchangeRecordMapper exchangeRecordMapper;
    private final UserMapper userMapper;
    
    @Override
    public List<VirtualProduct> listProducts() {
        // 查询所有上架的商品，按排序字段升序排列
        LambdaQueryWrapper<VirtualProduct> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VirtualProduct::getStatus, 1)
               .orderByAsc(VirtualProduct::getSortOrder);
        return virtualProductMapper.selectList(wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void exchangeProduct(Long userId, Long productId) {
        // 1. 查询用户信息
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 2. 查询商品信息
        VirtualProduct product = virtualProductMapper.selectById(productId);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        if (product.getStatus() == 0) {
            throw new RuntimeException("商品已下架");
        }
        
        // 3. 检查积分是否足够
        if (user.getPoints() < product.getPrice()) {
            throw new RuntimeException("积分不足，当前积分：" + user.getPoints() + "，需要：" + product.getPrice());
        }
        
        // 4. 检查是否已经拥有该商品（防止重复购买）
        String productType = product.getProductType();
        if ("tree".equals(productType) && user.getHasVirtualTree() == 1) {
            throw new RuntimeException("您已经拥有虚拟树，无需重复兑换");
        }
        if ("badge".equals(productType) && user.getVipBadge() == 1) {
            throw new RuntimeException("您已经拥有VIP勋章，无需重复兑换");
        }
        
        // 5. 扣除积分
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, userId)
                    .set(User::getPoints, user.getPoints() - product.getPrice());
        
        // 6. 根据商品类型，即买即用（直接修改用户字段）
        switch (productType) {
            case "theme":
                // 主题类商品，直接切换主题
                String themeName = product.getProductName().contains("绿") ? "green" : "dark";
                updateWrapper.set(User::getThemeStyle, themeName);
                break;
            case "tree":
                // 虚拟树，设置拥有标识
                updateWrapper.set(User::getHasVirtualTree, 1);
                break;
            case "badge":
                // VIP勋章，设置拥有标识
                updateWrapper.set(User::getVipBadge, 1);
                break;
            default:
                throw new RuntimeException("未知的商品类型");
        }
        
        userMapper.update(null, updateWrapper);
        
        // 7. 记录兑换日志
        ExchangeRecord record = new ExchangeRecord();
        record.setUserId(userId);
        record.setProductId(productId);
        record.setProductName(product.getProductName());
        record.setPrice(product.getPrice());
        record.setExchangeTime(LocalDateTime.now());
        exchangeRecordMapper.insert(record);
    }
    
    @Override
    public List<ExchangeRecord> getUserExchangeRecords(Long userId) {
        LambdaQueryWrapper<ExchangeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExchangeRecord::getUserId, userId)
               .orderByDesc(ExchangeRecord::getExchangeTime);
        return exchangeRecordMapper.selectList(wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeTheme(Long userId, String themeStyle) {
        // 验证主题样式
        if (!"default".equals(themeStyle) && !"green".equals(themeStyle) && !"dark".equals(themeStyle)) {
            throw new RuntimeException("不支持的主题样式");
        }
        
        // 查询用户信息
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 如果切换到非默认主题，需要检查是否已购买
        if (!"default".equals(themeStyle) && !"default".equals(user.getThemeStyle())) {
            // 查询用户是否购买过该主题
            LambdaQueryWrapper<ExchangeRecord> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ExchangeRecord::getUserId, userId);
            List<ExchangeRecord> records = exchangeRecordMapper.selectList(wrapper);
            
            boolean hasPurchased = false;
            for (ExchangeRecord record : records) {
                if (record.getProductName().contains("绿") && "green".equals(themeStyle)) {
                    hasPurchased = true;
                    break;
                }
                if (record.getProductName().contains("暗夜") && "dark".equals(themeStyle)) {
                    hasPurchased = true;
                    break;
                }
            }
            
            if (!hasPurchased) {
                throw new RuntimeException("您还未购买该主题，请先前往商城兑换");
            }
        }
        
        // 更新用户主题
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, userId)
                    .set(User::getThemeStyle, themeStyle);
        userMapper.update(null, updateWrapper);
    }
}
