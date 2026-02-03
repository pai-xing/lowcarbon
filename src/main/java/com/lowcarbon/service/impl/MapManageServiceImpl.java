package com.lowcarbon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lowcarbon.dto.MapPoiDTO;
import com.lowcarbon.entity.Footprint;
import com.lowcarbon.entity.MapPoi;
import com.lowcarbon.entity.User;
import com.lowcarbon.mapper.FootprintMapper;
import com.lowcarbon.mapper.MapPoiMapper;
import com.lowcarbon.mapper.UserMapper;
import com.lowcarbon.service.MapManageService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 地图管理服务实现
 */
@Service
public class MapManageServiceImpl implements MapManageService {
    
    @Autowired
    private FootprintMapper footprintMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private MapPoiMapper mapPoiMapper;
    
    @Override
    public List<Map<String, Object>> getAllUsersFootprints(LocalDate startDate, LocalDate endDate) {
        // 构建查询条件
        LambdaQueryWrapper<Footprint> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询有经纬度的记录
        wrapper.isNotNull(Footprint::getLatitude)
               .isNotNull(Footprint::getLongitude);
        
        // 日期范围过滤
        if (startDate != null) {
            wrapper.ge(Footprint::getRecordDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(Footprint::getRecordDate, endDate);
        }
        
        // 按日期降序
        wrapper.orderByDesc(Footprint::getRecordDate);
        
        List<Footprint> footprints = footprintMapper.selectList(wrapper);
        
        // 获取所有用户信息
        Set<Long> userIds = footprints.stream()
                .map(Footprint::getUserId)
                .collect(Collectors.toSet());
        
        Map<Long, String> userNameMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIds);
            userNameMap = users.stream()
                    .collect(Collectors.toMap(User::getId, User::getUsername));
        }
        
        // 组装返回数据
        List<Map<String, Object>> result = new ArrayList<>();
        for (Footprint footprint : footprints) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", footprint.getId());
            item.put("userId", footprint.getUserId());
            item.put("userName", userNameMap.getOrDefault(footprint.getUserId(), "未知用户"));
            item.put("behaviorType", footprint.getBehaviorType());
            item.put("behaviorName", footprint.getBehaviorName());
            item.put("latitude", footprint.getLatitude());
            item.put("longitude", footprint.getLongitude());
            item.put("address", footprint.getAddress());
            item.put("recordDate", footprint.getRecordDate() != null ? footprint.getRecordDate().toString() : "");
            item.put("carbonReduction", footprint.getReductionAmount());
            item.put("remark", footprint.getRemark());
            result.add(item);
        }
        
        return result;
    }
    
    @Override
    public List<MapPoi> getAllPois() {
        LambdaQueryWrapper<MapPoi> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MapPoi::getStatus, 1) // 只返回启用状态的
               .orderByDesc(MapPoi::getCreateTime);
        return mapPoiMapper.selectList(wrapper);
    }
    
    @Override
    public MapPoi createPoi(MapPoiDTO dto) {
        MapPoi poi = new MapPoi();
        BeanUtils.copyProperties(dto, poi);
        poi.setStatus(1); // 默认启用
        mapPoiMapper.insert(poi);
        return poi;
    }
    
    @Override
    public MapPoi updatePoi(Long id, MapPoiDTO dto) {
        MapPoi poi = mapPoiMapper.selectById(id);
        if (poi == null) {
            throw new RuntimeException("地点不存在");
        }
        BeanUtils.copyProperties(dto, poi);
        mapPoiMapper.updateById(poi);
        return poi;
    }
    
    @Override
    public void deletePoi(Long id) {
        MapPoi poi = mapPoiMapper.selectById(id);
        if (poi == null) {
            throw new RuntimeException("地点不存在");
        }
        mapPoiMapper.deleteById(id);
    }
}
