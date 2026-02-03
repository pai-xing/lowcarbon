package com.lowcarbon.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 地图预设点（POI - Point of Interest）实体
 */
@Data
@TableName("tb_map_poi")
public class MapPoi {
    
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 地点名称
     */
    private String name;
    
    /**
     * 纬度
     */
    private BigDecimal latitude;
    
    /**
     * 经度
     */
    private BigDecimal longitude;
    
    /**
     * 详细地址
     */
    private String address;
    
    /**
     * POI类型：custom-自定义, library-图书馆, canteen-食堂, gym-体育馆等
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
    
    /**
     * 状态：0-禁用 1-启用
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
