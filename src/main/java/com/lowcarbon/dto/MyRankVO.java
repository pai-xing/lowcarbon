package com.lowcarbon.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 我的排名VO
 */
@Data
public class MyRankVO {
    /**
     * 我的排名（0表示未上榜）
     */
    private Integer rank;
    
    /**
     * 我的积分
     */
    private Integer points;
    
    /**
     * 我的总减排量
     */
    private BigDecimal totalReduction;
    
    /**
     * 总用户数
     */
    private Long totalUsers;
}
