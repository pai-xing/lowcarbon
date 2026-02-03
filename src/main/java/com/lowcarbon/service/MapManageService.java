package com.lowcarbon.service;

import com.lowcarbon.dto.MapPoiDTO;
import com.lowcarbon.entity.MapPoi;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 地图管理服务接口
 */
public interface MapManageService {
    
    /**
     * 获取所有用户的足迹点（管理员用）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 所有用户的足迹点列表
     */
    List<Map<String, Object>> getAllUsersFootprints(LocalDate startDate, LocalDate endDate);
    
    /**
     * 获取所有预设地点
     * @return 预设地点列表
     */
    List<MapPoi> getAllPois();
    
    /**
     * 创建预设地点
     * @param dto 地点信息
     * @return 创建的地点
     */
    MapPoi createPoi(MapPoiDTO dto);
    
    /**
     * 更新预设地点
     * @param id 地点ID
     * @param dto 地点信息
     * @return 更新后的地点
     */
    MapPoi updatePoi(Long id, MapPoiDTO dto);
    
    /**
     * 删除预设地点
     * @param id 地点ID
     */
    void deletePoi(Long id);
}
