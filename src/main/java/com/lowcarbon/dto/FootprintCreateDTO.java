package com.lowcarbon.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class FootprintCreateDTO {
    @NotBlank(message = "行为类型不能为空")
    private String behaviorType;
    
    @NotBlank(message = "行为名称不能为空")
    private String behaviorName;
    
    @NotNull(message = "数据值不能为空")
    @DecimalMin(value = "0.01", message = "数据值必须≥0.01")
    private BigDecimal dataValue;
    
    @NotNull(message = "记录日期不能为空")
    private LocalDate recordDate;
    
    private String remark;
}
