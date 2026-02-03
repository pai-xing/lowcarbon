package com.lowcarbon.dto;

import lombok.Data;
import java.util.List;

/**
 * 排行榜响应VO
 */
@Data
public class LeaderboardResponseVO {
    /**
     * 排行榜列表
     */
    private List<LeaderboardVO> list;
    
    /**
     * 我的排名信息
     */
    private MyRankVO myRank;
    
    /**
     * 榜单类型（total-总榜，week-周榜，month-月榜，activity-活动榜）
     */
    private String type;
}
