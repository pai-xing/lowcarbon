package com.lowcarbon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lowcarbon.dto.RankingVO;
import com.lowcarbon.entity.Activity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 活动Mapper接口
 */
@Mapper
public interface ActivityMapper extends BaseMapper<Activity> {
    
    /**
     * 获取活动排行榜
     * 核心逻辑：复用现有的碳足迹数据，按时间范围统计
     */
    @Select("SELECT " +
            "u.id as userId, " +
            "u.username, " +
            "u.nickname, " +
            "u.avatar, " +
            "COALESCE(SUM(f.reduction_amount), 0) as totalReduction, " +
            "COALESCE(SUM(f.points_earned), 0) as totalPoints " +
            "FROM tb_user u " +
            "INNER JOIN tb_activity_join aj ON u.id = aj.user_id " +
            "LEFT JOIN tb_footprint f ON u.id = f.user_id " +
            "AND f.create_time BETWEEN #{startTime} AND #{endTime} " +
            "WHERE aj.activity_id = #{activityId} " +
            "GROUP BY u.id, u.username, u.nickname, u.avatar " +
            "ORDER BY totalReduction DESC " +
            "LIMIT #{limit}")
    List<RankingVO> getActivityRanking(@Param("activityId") Long activityId,
                                       @Param("startTime") LocalDateTime startTime,
                                       @Param("endTime") LocalDateTime endTime,
                                       @Param("limit") Integer limit);
    
    /**
     * 获取用户在活动中的排名信息
     */
    @Select("SELECT " +
            "u.id as userId, " +
            "u.username, " +
            "u.nickname, " +
            "u.avatar, " +
            "COALESCE(SUM(f.reduction_amount), 0) as totalReduction, " +
            "COALESCE(SUM(f.points_earned), 0) as totalPoints " +
            "FROM tb_user u " +
            "INNER JOIN tb_activity_join aj ON u.id = aj.user_id " +
            "LEFT JOIN tb_footprint f ON u.id = f.user_id " +
            "AND f.create_time BETWEEN #{startTime} AND #{endTime} " +
            "WHERE aj.activity_id = #{activityId} AND u.id = #{userId} " +
            "GROUP BY u.id, u.username, u.nickname, u.avatar")
    RankingVO getUserRanking(@Param("activityId") Long activityId,
                            @Param("userId") Long userId,
                            @Param("startTime") LocalDateTime startTime,
                            @Param("endTime") LocalDateTime endTime);
}
