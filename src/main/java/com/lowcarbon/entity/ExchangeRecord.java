package com.lowcarbon.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分兑换记录实体类
 */
@Data
@TableName("tb_exchange_record")
public class ExchangeRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;          // 用户ID
    private Long productId;       // 商品ID
    private String productName;   // 商品名称
    private Integer price;        // 消耗积分
    private LocalDateTime exchangeTime;  // 兑换时间
}
