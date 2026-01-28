package com.lowcarbon.service;

import com.lowcarbon.dto.ActivityVO;
import com.lowcarbon.dto.RankingVO;
import com.lowcarbon.entity.Activity;

import java.util.List;

/**
 * 活动服务接口
 */
public interface ActivityService {
    
    /**
     * 创建活动（管理员）
     */
    void createActivity(Activity activity);
    
    /**
     * 更新活动（管理员）
     */
    void updateActivity(Activity activity);
    
    /**
     * 删除活动（管理员）
     */
    void deleteActivity(Long id);
    
    /**
     * 获取活动列表
     * @param status 状态筛选：null-全部，0-未开始，1-进行中，2-已结束
     * @param userId 当前用户ID，用于判断是否已参加
     */
    List<ActivityVO> getActivityList(Integer status, Long userId);
    
    /**
     * 获取活动详情
     */
    ActivityVO getActivityDetail(Long id, Long userId);
    
    /**
     * 参加活动
     */
    void joinActivity(Long activityId, Long userId);
    
    /**
     * 获取活动排行榜
     * @param activityId 活动ID
     * @param limit 返回数量，默认100
     */
    List<RankingVO> getActivityRanking(Long activityId, Integer limit);
    
    /**
     * 获取用户在活动中的排名
     */
    RankingVO getUserRanking(Long activityId, Long userId);
    
    /**
     * 更新活动状态（定时任务调用）
     */
    void updateActivityStatus();
}
