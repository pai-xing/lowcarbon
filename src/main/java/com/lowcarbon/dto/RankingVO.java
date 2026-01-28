package com.lowcarbon.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 排行榜视图对象
 */
@Data
public class RankingVO {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 昵称
     */
    private String nickname;
    
    /**
     * 头像
     */
    private String avatar;
    
    /**
     * 总减排量(kg)
     */
    private BigDecimal totalReduction;
    
    /**
     * 总积分
     */
    private Integer totalPoints;
    
    /**
     * 排名
     */
    private Integer rank;
}
