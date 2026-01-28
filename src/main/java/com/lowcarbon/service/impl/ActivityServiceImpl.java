package com.lowcarbon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lowcarbon.dto.ActivityVO;
import com.lowcarbon.dto.RankingVO;
import com.lowcarbon.entity.Activity;
import com.lowcarbon.entity.ActivityJoin;
import com.lowcarbon.mapper.ActivityJoinMapper;
import com.lowcarbon.mapper.ActivityMapper;
import com.lowcarbon.service.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 活动服务实现类
 */
@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {
    
    private final ActivityMapper activityMapper;
    private final ActivityJoinMapper activityJoinMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createActivity(Activity activity) {
        // 根据开始时间自动设置状态
        LocalDateTime now = LocalDateTime.now();
        if (activity.getStartTime().isAfter(now)) {
            activity.setStatus(0); // 未开始
        } else if (activity.getEndTime().isBefore(now)) {
            activity.setStatus(2); // 已结束
        } else {
            activity.setStatus(1); // 进行中
        }
        
        activityMapper.insert(activity);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateActivity(Activity activity) {
        activityMapper.updateById(activity);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteActivity(Long id) {
        activityMapper.deleteById(id);
        // 删除相关参与记录
        LambdaQueryWrapper<ActivityJoin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivityJoin::getActivityId, id);
        activityJoinMapper.delete(wrapper);
    }
    
    @Override
    public List<ActivityVO> getActivityList(Integer status, Long userId) {
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Activity::getStatus, status);
        }
        wrapper.orderByDesc(Activity::getCreateTime);
        
        List<Activity> activities = activityMapper.selectList(wrapper);
        
        return activities.stream().map(activity -> {
            ActivityVO vo = new ActivityVO();
            BeanUtils.copyProperties(activity, vo);
            
            // 设置状态文本
            vo.setStatusText(getStatusText(activity.getStatus()));
            
            // 统计参与人数
            LambdaQueryWrapper<ActivityJoin> joinWrapper = new LambdaQueryWrapper<>();
            joinWrapper.eq(ActivityJoin::getActivityId, activity.getId());
            Long count = activityJoinMapper.selectCount(joinWrapper);
            vo.setParticipantCount(count.intValue());
            
            // 判断当前用户是否已参加
            if (userId != null) {
                LambdaQueryWrapper<ActivityJoin> userJoinWrapper = new LambdaQueryWrapper<>();
                userJoinWrapper.eq(ActivityJoin::getActivityId, activity.getId())
                              .eq(ActivityJoin::getUserId, userId);
                Long userJoinCount = activityJoinMapper.selectCount(userJoinWrapper);
                vo.setHasJoined(userJoinCount > 0);
            } else {
                vo.setHasJoined(false);
            }
            
            return vo;
        }).collect(Collectors.toList());
    }
    
    @Override
    public ActivityVO getActivityDetail(Long id, Long userId) {
        Activity activity = activityMapper.selectById(id);
        if (activity == null) {
            throw new RuntimeException("活动不存在");
        }
        
        ActivityVO vo = new ActivityVO();
        BeanUtils.copyProperties(activity, vo);
        
        // 设置状态文本
        vo.setStatusText(getStatusText(activity.getStatus()));
        
        // 统计参与人数
        LambdaQueryWrapper<ActivityJoin> joinWrapper = new LambdaQueryWrapper<>();
        joinWrapper.eq(ActivityJoin::getActivityId, id);
        Long count = activityJoinMapper.selectCount(joinWrapper);
        vo.setParticipantCount(count.intValue());
        
        // 判断当前用户是否已参加
        if (userId != null) {
            LambdaQueryWrapper<ActivityJoin> userJoinWrapper = new LambdaQueryWrapper<>();
            userJoinWrapper.eq(ActivityJoin::getActivityId, id)
                          .eq(ActivityJoin::getUserId, userId);
            Long userJoinCount = activityJoinMapper.selectCount(userJoinWrapper);
            vo.setHasJoined(userJoinCount > 0);
        } else {
            vo.setHasJoined(false);
        }
        
        return vo;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void joinActivity(Long activityId, Long userId) {
        // 检查活动是否存在
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new RuntimeException("活动不存在");
        }
        
        // 检查活动状态
        if (activity.getStatus() == 2) {
            throw new RuntimeException("活动已结束，无法参加");
        }
        
        // 检查是否已参加
        LambdaQueryWrapper<ActivityJoin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivityJoin::getActivityId, activityId)
               .eq(ActivityJoin::getUserId, userId);
        Long count = activityJoinMapper.selectCount(wrapper);
        if (count > 0) {
            throw new RuntimeException("您已参加该活动");
        }
        
        // 创建参与记录
        ActivityJoin join = new ActivityJoin();
        join.setActivityId(activityId);
        join.setUserId(userId);
        join.setJoinTime(LocalDateTime.now());
        activityJoinMapper.insert(join);
    }
    
    @Override
    public List<RankingVO> getActivityRanking(Long activityId, Integer limit) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new RuntimeException("活动不存在");
        }
        
        if (limit == null || limit <= 0) {
            limit = 100;
        }
        
        // 调用Mapper的排行榜查询方法
        List<RankingVO> rankings = activityMapper.getActivityRanking(
            activityId,
            activity.getStartTime(),
            activity.getEndTime(),
            limit
        );
        
        // 设置排名
        for (int i = 0; i < rankings.size(); i++) {
            rankings.get(i).setRank(i + 1);
        }
        
        return rankings;
    }
    
    @Override
    public RankingVO getUserRanking(Long activityId, Long userId) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new RuntimeException("活动不存在");
        }
        
        // 检查用户是否参加活动
        LambdaQueryWrapper<ActivityJoin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivityJoin::getActivityId, activityId)
               .eq(ActivityJoin::getUserId, userId);
        Long count = activityJoinMapper.selectCount(wrapper);
        if (count == 0) {
            throw new RuntimeException("您未参加该活动");
        }
        
        // 获取用户排名信息
        RankingVO userRanking = activityMapper.getUserRanking(
            activityId,
            userId,
            activity.getStartTime(),
            activity.getEndTime()
        );
        
        if (userRanking == null) {
            throw new RuntimeException("未找到用户排名信息");
        }
        
        // 计算用户排名（通过比较减排量）
        List<RankingVO> allRankings = activityMapper.getActivityRanking(
            activityId,
            activity.getStartTime(),
            activity.getEndTime(),
            10000 // 获取所有参与者
        );
        
        for (int i = 0; i < allRankings.size(); i++) {
            if (allRankings.get(i).getUserId().equals(userId)) {
                userRanking.setRank(i + 1);
                break;
            }
        }
        
        return userRanking;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateActivityStatus() {
        LocalDateTime now = LocalDateTime.now();
        
        // 更新未开始的活动为进行中
        LambdaQueryWrapper<Activity> notStartedWrapper = new LambdaQueryWrapper<>();
        notStartedWrapper.eq(Activity::getStatus, 0)
                        .le(Activity::getStartTime, now)
                        .ge(Activity::getEndTime, now);
        List<Activity> notStartedActivities = activityMapper.selectList(notStartedWrapper);
        notStartedActivities.forEach(activity -> {
            activity.setStatus(1);
            activityMapper.updateById(activity);
        });
        
        // 更新进行中的活动为已结束
        LambdaQueryWrapper<Activity> ongoingWrapper = new LambdaQueryWrapper<>();
        ongoingWrapper.eq(Activity::getStatus, 1)
                     .lt(Activity::getEndTime, now);
        List<Activity> ongoingActivities = activityMapper.selectList(ongoingWrapper);
        ongoingActivities.forEach(activity -> {
            activity.setStatus(2);
            activityMapper.updateById(activity);
        });
    }
    
    /**
     * 获取状态文本
     */
    private String getStatusText(Integer status) {
        switch (status) {
            case 0:
                return "未开始";
            case 1:
                return "进行中";
            case 2:
                return "已结束";
            default:
                return "未知";
        }
    }
}
