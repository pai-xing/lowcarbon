package com.lowcarbon.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 活动视图对象
 */
@Data
public class ActivityVO {
    
    /**
     * 活动ID
     */
    private Long id;
    
    /**
     * 活动标题
     */
    private String title;
    
    /**
     * 活动描述
     */
    private String description;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 状态：0-未开始，1-进行中，2-已结束
     */
    private Integer status;
    
    /**
     * 状态文本
     */
    private String statusText;
    
    /**
     * 目标减排量(kg)
     */
    private BigDecimal targetReduction;
    
    /**
     * 参与人数
     */
    private Integer participantCount;
    
    /**
     * 当前用户是否已参加
     */
    private Boolean hasJoined;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
