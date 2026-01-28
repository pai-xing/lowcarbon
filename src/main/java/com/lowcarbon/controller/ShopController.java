package com.lowcarbon.controller;

import com.lowcarbon.common.Result;
import com.lowcarbon.entity.ExchangeRecord;
import com.lowcarbon.entity.User;
import com.lowcarbon.entity.VirtualProduct;
import com.lowcarbon.service.ShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 积分商城Controller
 */
@Tag(name = "积分商城接口")
@RestController
@RequestMapping("/shop")
@RequiredArgsConstructor
public class ShopController {
    
    private final ShopService shopService;
    
    @Operation(summary = "获取商品列表")
    @GetMapping("/products")
    public Result<List<VirtualProduct>> listProducts() {
        List<VirtualProduct> products = shopService.listProducts();
        return Result.success(products);
    }
    
    @Operation(summary = "兑换商品")
    @PostMapping("/exchange/{productId}")
    public Result<String> exchangeProduct(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long productId) {
        shopService.exchangeProduct(userId, productId);
        return Result.success("兑换成功");
    }
    
    @Operation(summary = "获取兑换记录")
    @GetMapping("/records")
    public Result<List<ExchangeRecord>> getExchangeRecords(@RequestAttribute("userId") Long userId) {
        List<ExchangeRecord> records = shopService.getUserExchangeRecords(userId);
        return Result.success(records);
    }
    
    @Operation(summary = "切换主题")
    @PostMapping("/theme/{themeStyle}")
    public Result<String> changeTheme(
            @RequestAttribute("userId") Long userId,
            @PathVariable String themeStyle) {
        shopService.changeTheme(userId, themeStyle);
        return Result.success("主题切换成功");
    }
}
