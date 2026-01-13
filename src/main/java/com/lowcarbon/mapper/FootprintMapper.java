package com.lowcarbon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lowcarbon.entity.Footprint;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface FootprintMapper extends BaseMapper<Footprint> {
    
    /**
     * 获取用户指定日期范围内的碳足迹统计
     */
    @Select("SELECT SUM(reduction_amount) as totalReduction, SUM(points_earned) as totalPoints " +
            "FROM tb_footprint " +
            "WHERE user_id = #{userId} AND record_date BETWEEN #{startDate} AND #{endDate}")
    Map<String, Object> getStatistics(Long userId, LocalDate startDate, LocalDate endDate);
    
    /**
     * 获取用户按行为类型的统计
     */
    @Select("SELECT behavior_type, behavior_name, COUNT(*) as count, " +
            "SUM(reduction_amount) as totalReduction, SUM(points_earned) as totalPoints " +
            "FROM tb_footprint " +
            "WHERE user_id = #{userId} " +
            "GROUP BY behavior_type, behavior_name " +
            "ORDER BY totalReduction DESC")
    List<Map<String, Object>> getStatisticsByBehaviorType(Long userId);
    
    /**
     * 获取用户按日期的统计（用于趋势图）
     */
    @Select("SELECT record_date, SUM(reduction_amount) as dailyReduction, SUM(points_earned) as dailyPoints " +
            "FROM tb_footprint " +
            "WHERE user_id = #{userId} AND record_date BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY record_date " +
            "ORDER BY record_date")
    List<Map<String, Object>> getDailyStatistics(Long userId, LocalDate startDate, LocalDate endDate);
    
    /**
     * 判断当日是否已存在指定行为的打卡（用于幂等校验）
     */
    @Select("SELECT COUNT(*) FROM tb_footprint " +
            "WHERE user_id = #{userId} AND behavior_type = #{behaviorType} AND record_date = #{recordDate}")
    int existsCheckin(Long userId, String behaviorType, LocalDate recordDate);
    
    /**
     * 获取指定月份内的打卡日历（按日期聚合：次数与行为类型列表）
     * types 字段为行为类型的逗号分隔字符串（使用 GROUP_CONCAT）
     */
    @Select("SELECT record_date, COUNT(*) as count, GROUP_CONCAT(DISTINCT behavior_type) as types " +
            "FROM tb_footprint " +
            "WHERE user_id = #{userId} AND record_date BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY record_date " +
            "ORDER BY record_date")
    List<Map<String, Object>> getCheckinCalendar(Long userId, LocalDate startDate, LocalDate endDate);
    
    /**
     * 获取累计打卡数（用户维度）
     */
    @Select("SELECT COUNT(*) FROM tb_footprint WHERE user_id = #{userId}")
    Long getTotalCheckins(Long userId);
    
    /**
     * 获取本月已打卡天数（去重日期）
     */
    @Select("SELECT COUNT(DISTINCT record_date) FROM tb_footprint " +
            "WHERE user_id = #{userId} AND record_date BETWEEN #{startDate} AND #{endDate}")
    Long getMonthlyCheckedDays(Long userId, LocalDate startDate, LocalDate endDate);
    
    /**
     * 判断某日是否有任意打卡（用于 streak 计算）
     */
    @Select("SELECT COUNT(*) FROM tb_footprint WHERE user_id = #{userId} AND record_date = #{recordDate}")
    int existsAnyOnDate(Long userId, LocalDate recordDate);
}
