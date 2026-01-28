package com.lowcarbon.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 虚拟商品实体类
 */
@Data
@TableName("tb_virtual_product")
public class VirtualProduct {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String productName;   // 商品名称
    private String productType;   // 商品类型：theme/tree/badge
    private String description;   // 商品描述
    private Integer price;        // 积分价格
    private String iconUrl;       // 商品图标
    private Integer status;       // 状态：0-下架，1-上架
    private Integer sortOrder;    // 排序顺序
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
