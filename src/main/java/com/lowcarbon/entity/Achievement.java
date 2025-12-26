package com.lowcarbon.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_achievement")
public class Achievement {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Integer type; // 1-获得勋章, 2-积分兑换
    private String title;
    private String description;
    private Integer pointsChange;
    private String iconUrl;
    private LocalDateTime createTime;
}

