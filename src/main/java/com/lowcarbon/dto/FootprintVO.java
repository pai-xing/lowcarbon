package com.lowcarbon.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class FootprintVO {
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
