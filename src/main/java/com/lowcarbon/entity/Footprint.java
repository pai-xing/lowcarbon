package com.lowcarbon.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("tb_footprint")
public class Footprint {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String behaviorType;
    private String behaviorName;
    private BigDecimal coefficient;
    private BigDecimal dataValue;
    private BigDecimal reductionAmount;
    private Integer pointsEarned;
    private LocalDate recordDate;
    private String remark;
    private LocalDateTime createTime;
}
