package com.lowcarbon.service;

import com.lowcarbon.dto.LeaderboardResponseVO;

/**
 * 排行榜服务接口
 */
public interface LeaderboardService {
    
    /**
     * 获取排行榜
     * @param type 榜单类型（total-总榜，week-周榜，month-月榜，activity-活动榜）
     * @param activityId 活动ID（仅type=activity时需要）
     * @param limit 返回条数，默认50
     * @param userId 当前用户ID（用于查询"我的排名"）
     * @return 排行榜数据
     */
    LeaderboardResponseVO getLeaderboard(String type, Long activityId, Integer limit, Long userId);
}
