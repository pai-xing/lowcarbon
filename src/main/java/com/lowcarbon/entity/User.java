package com.lowcarbon.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tb_user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String avatar;
    private String bio;
    private Integer points;
    private BigDecimal totalReduction;
    private String role;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // 虚拟商品字段
    private String themeStyle;      // 主题皮肤：default/green/dark
    private Integer hasVirtualTree; // 是否拥有虚拟树：0-无，1-有
    private Integer vipBadge;       // 是否拥有VIP标识：0-无，1-有
}
