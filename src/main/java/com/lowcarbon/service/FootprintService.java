package com.lowcarbon.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lowcarbon.dto.FootprintCreateDTO;
import com.lowcarbon.dto.FootprintVO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface FootprintService {
    
    /**
     * 创建碳足迹记录
     */
    FootprintVO createFootprint(FootprintCreateDTO createDTO, Long userId);
    
    /**
     * 获取用户碳足迹记录列表（分页）
     */
    IPage<FootprintVO> getFootprintList(Long userId, Integer pageNum, Integer pageSize, LocalDate startDate, LocalDate endDate);
    
    /**
     * 获取用户碳足迹统计数据
     */
    Map<String, Object> getStatistics(Long userId, LocalDate startDate, LocalDate endDate);
    
    /**
     * 获取用户按行为类型的统计
     */
    List<Map<String, Object>> getStatisticsByBehaviorType(Long userId);
    
    /**
     * 获取用户按日期的统计（用于趋势图）
     */
    List<Map<String, Object>> getDailyStatistics(Long userId, LocalDate startDate, LocalDate endDate);
    
    /**
     * 更新碳足迹记录（需根据新数据重新计算减排与积分，并修正用户累计）
     */
    FootprintVO updateFootprint(Long id, FootprintCreateDTO updateDTO, Long userId);
    
    /**
     * 标准打卡（单位为“次”的行为，dataValue=1，recordDate=当天，含幂等校验与积分计算）
     */
    FootprintVO checkin(String behaviorType, Long userId, String remark);
    
    /**
     * 获取指定月份的打卡日历（日期维度聚合：是否打卡、次数、行为类型列表）
     */
    List<Map<String, Object>> getCheckinCalendar(Long userId, LocalDate monthStart, LocalDate monthEnd);
    
    /**
     * 获取用户打卡统计（连续打卡天数streak、累计打卡数totalCheckins、本月达成率monthlyRate）
     */
    Map<String, Object> getCheckinStats(Long userId);
    
    /**
     * 删除碳足迹记录
     */
    void deleteFootprint(Long id, Long userId);
}
