package com.lowcarbon.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 排行榜VO
 */
@Data
public class LeaderboardVO {
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
     * 积分
     */
    private Integer points;
    
    /**
     * 总减排量
     */
    private BigDecimal totalReduction;
    
    /**
     * 排名
     */
    private Integer rank;
    
    /**
     * VIP标识
     */
    private Integer vipBadge;
}
