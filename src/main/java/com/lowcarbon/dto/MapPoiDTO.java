package com.lowcarbon.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 地图预设点DTO
 */
@Data
public class MapPoiDTO {
    
    /**
     * 地点名称
     */
    @NotBlank(message = "地点名称不能为空")
    private String name;
    
    /**
     * 纬度
     */
    @NotNull(message = "纬度不能为空")
    private BigDecimal latitude;
    
    /**
     * 经度
     */
    @NotNull(message = "经度不能为空")
    private BigDecimal longitude;
    
    /**
     * 详细地址
     */
    private String address;
    
    /**
     * POI类型
     */
    private String poiType;
    
    /**
     * 描述信息
     */
    private String description;
    
    /**
     * 图标标识
     */
    private String icon;
}
