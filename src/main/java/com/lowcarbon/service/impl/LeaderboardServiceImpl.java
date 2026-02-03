package com.lowcarbon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lowcarbon.dto.LeaderboardResponseVO;
import com.lowcarbon.dto.LeaderboardVO;
import com.lowcarbon.dto.MyRankVO;
import com.lowcarbon.entity.Activity;
import com.lowcarbon.entity.Footprint;
import com.lowcarbon.entity.User;
import com.lowcarbon.mapper.ActivityMapper;
import com.lowcarbon.mapper.FootprintMapper;
import com.lowcarbon.mapper.UserMapper;
import com.lowcarbon.service.LeaderboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 排行榜服务实现
 */
@Service
public class LeaderboardServiceImpl implements LeaderboardService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private FootprintMapper footprintMapper;
    
    @Autowired
    private ActivityMapper activityMapper;
    
    @Override
    public LeaderboardResponseVO getLeaderboard(String type, Long activityId, Integer limit, Long userId) {
        if (limit == null || limit <= 0) {
            limit = 50;
        }
        
        LeaderboardResponseVO response = new LeaderboardResponseVO();
        response.setType(type);
        
        List<LeaderboardVO> leaderboardList;
        
        // 根据类型查询不同的排行榜
        switch (type) {
            case "total":
                leaderboardList = getTotalLeaderboard(limit);
                break;
            case "week":
                leaderboardList = getWeekLeaderboard(limit);
                break;
            case "month":
                leaderboardList = getMonthLeaderboard(limit);
                break;
            case "activity":
                if (activityId == null) {
                    throw new RuntimeException("活动榜需要提供活动ID");
                }
                leaderboardList = getActivityLeaderboard(activityId, limit);
                break;
            default:
                throw new RuntimeException("不支持的榜单类型: " + type);
        }
        
        response.setList(leaderboardList);
        
        // 查询当前用户的排名
        if (userId != null) {
            MyRankVO myRank = getMyRank(type, activityId, userId);
            response.setMyRank(myRank);
        }
        
        return response;
    }
    
    /**
     * 总榜（基于用户表的冗余字段）
     */
    private List<LeaderboardVO> getTotalLeaderboard(Integer limit) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getStatus, 1) // 只查询正常状态的用户
                .orderByDesc(User::getPoints)
                .last("LIMIT " + limit);
        
        List<User> users = userMapper.selectList(wrapper);
        
        List<LeaderboardVO> result = new ArrayList<>();
        int rank = 1;
        for (User user : users) {
            LeaderboardVO vo = new LeaderboardVO();
            vo.setUserId(user.getId());
            vo.setUsername(user.getUsername());
            vo.setNickname(user.getNickname());
            vo.setAvatar(user.getAvatar());
            vo.setPoints(user.getPoints());
            vo.setTotalReduction(user.getTotalReduction());
            vo.setRank(rank++);
            vo.setVipBadge(user.getVipBadge());
            result.add(vo);
        }
        
        return result;
    }
    
    /**
     * 周榜（查询最近7天的碳足迹聚合）
     */
    private List<LeaderboardVO> getWeekLeaderboard(Integer limit) {
        LocalDate weekAgo = LocalDate.now().minusDays(7);
        return getTimePeriodLeaderboard(weekAgo, null, limit);
    }
    
    /**
     * 月榜（查询本月的碳足迹聚合）
     */
    private List<LeaderboardVO> getMonthLeaderboard(Integer limit) {
        LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
        return getTimePeriodLeaderboard(monthStart, null, limit);
    }
    
    /**
     * 活动榜（查询活动时间范围内的碳足迹聚合）
     */
    private List<LeaderboardVO> getActivityLeaderboard(Long activityId, Integer limit) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new RuntimeException("活动不存在");
        }
        
        LocalDate startDate = activity.getStartTime().toLocalDate();
        LocalDate endDate = activity.getEndTime().toLocalDate();
        
        return getTimePeriodLeaderboard(startDate, endDate, limit);
    }
    
    /**
     * 时间段排行榜通用方法
     */
    private List<LeaderboardVO> getTimePeriodLeaderboard(LocalDate startDate, LocalDate endDate, Integer limit) {
        // 查询时间段内的碳足迹记录，按用户分组聚合
        LambdaQueryWrapper<Footprint> wrapper = new LambdaQueryWrapper<>();
        
        if (startDate != null) {
            wrapper.ge(Footprint::getRecordDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(Footprint::getRecordDate, endDate);
        }
        
        List<Footprint> footprints = footprintMapper.selectList(wrapper);
        
        // 按用户ID聚合
        Map<Long, UserStats> userStatsMap = new HashMap<>();
        for (Footprint footprint : footprints) {
            Long uid = footprint.getUserId();
            UserStats stats = userStatsMap.computeIfAbsent(uid, k -> new UserStats());
            stats.points += footprint.getPointsEarned();
            stats.reduction += footprint.getReductionAmount().doubleValue();
        }
        
        // 转换为列表并排序
        List<Map.Entry<Long, UserStats>> sortedEntries = userStatsMap.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue().points, e1.getValue().points))
                .limit(limit)
                .collect(Collectors.toList());
        
        // 查询用户信息并组装结果
        List<LeaderboardVO> result = new ArrayList<>();
        int rank = 1;
        for (Map.Entry<Long, UserStats> entry : sortedEntries) {
            Long uid = entry.getKey();
            UserStats stats = entry.getValue();
            
            User user = userMapper.selectById(uid);
            if (user != null && user.getStatus() == 1) {
                LeaderboardVO vo = new LeaderboardVO();
                vo.setUserId(uid);
                vo.setUsername(user.getUsername());
                vo.setNickname(user.getNickname());
                vo.setAvatar(user.getAvatar());
                vo.setPoints(stats.points);
                vo.setTotalReduction(BigDecimal.valueOf(stats.reduction));
                vo.setRank(rank++);
                vo.setVipBadge(user.getVipBadge());
                result.add(vo);
            }
        }
        
        return result;
    }
    
    /**
     * 获取当前用户的排名信息
     */
    private MyRankVO getMyRank(String type, Long activityId, Long userId) {
        MyRankVO myRank = new MyRankVO();
        
        User currentUser = userMapper.selectById(userId);
        if (currentUser == null) {
            myRank.setRank(0);
            myRank.setPoints(0);
            myRank.setTotalReduction(BigDecimal.ZERO);
            return myRank;
        }
        
        // 查询总用户数
        LambdaQueryWrapper<User> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(User::getStatus, 1);
        Long totalUsers = userMapper.selectCount(countWrapper);
        myRank.setTotalUsers(totalUsers);
        
        switch (type) {
            case "total":
                myRank.setPoints(currentUser.getPoints());
                myRank.setTotalReduction(currentUser.getTotalReduction());
                // 查询排名
                LambdaQueryWrapper<User> rankWrapper = new LambdaQueryWrapper<>();
                rankWrapper.eq(User::getStatus, 1)
                        .gt(User::getPoints, currentUser.getPoints());
                Long betterCount = userMapper.selectCount(rankWrapper);
                myRank.setRank(betterCount.intValue() + 1);
                break;
                
            case "week":
                LocalDate weekAgo = LocalDate.now().minusDays(7);
                setMyRankForPeriod(myRank, userId, weekAgo, null);
                break;
                
            case "month":
                LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
                setMyRankForPeriod(myRank, userId, monthStart, null);
                break;
                
            case "activity":
                Activity activity = activityMapper.selectById(activityId);
                if (activity != null) {
                    LocalDate startDate = activity.getStartTime().toLocalDate();
                    LocalDate endDate = activity.getEndTime().toLocalDate();
                    setMyRankForPeriod(myRank, userId, startDate, endDate);
                }
                break;
        }
        
        return myRank;
    }
    
    /**
     * 设置时间段内的个人排名
     */
    private void setMyRankForPeriod(MyRankVO myRank, Long userId, LocalDate startDate, LocalDate endDate) {
        // 计算用户在该时间段内的积分和减排量
        LambdaQueryWrapper<Footprint> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Footprint::getUserId, userId);
        
        if (startDate != null) {
            wrapper.ge(Footprint::getRecordDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(Footprint::getRecordDate, endDate);
        }
        
        List<Footprint> myFootprints = footprintMapper.selectList(wrapper);
        
        int tempPoints = 0;
        BigDecimal myReduction = BigDecimal.ZERO;
        for (Footprint fp : myFootprints) {
            tempPoints += fp.getPointsEarned();
            myReduction = myReduction.add(fp.getReductionAmount());
        }
        
        final int myPoints = tempPoints; // 声明为final以便在lambda中使用
        myRank.setPoints(myPoints);
        myRank.setTotalReduction(myReduction);
        
        // 计算排名：查询有多少用户的积分比我高
        LambdaQueryWrapper<Footprint> allWrapper = new LambdaQueryWrapper<>();
        if (startDate != null) {
            allWrapper.ge(Footprint::getRecordDate, startDate);
        }
        if (endDate != null) {
            allWrapper.le(Footprint::getRecordDate, endDate);
        }
        
        List<Footprint> allFootprints = footprintMapper.selectList(allWrapper);
        
        // 聚合所有用户的积分
        Map<Long, Integer> userPointsMap = new HashMap<>();
        for (Footprint fp : allFootprints) {
            Long uid = fp.getUserId();
            Integer points = fp.getPointsEarned();
            userPointsMap.put(uid, userPointsMap.getOrDefault(uid, 0) + points);
        }
        
        // 统计比我积分高的用户数
        long betterCount = userPointsMap.values().stream()
                .filter(points -> points > myPoints)
                .count();
        
        myRank.setRank((int) betterCount + 1);
    }
    
    /**
     * 用户统计内部类
     */
    private static class UserStats {
        int points = 0;
        Double reduction = 0.0;
    }
}
