package com.lowcarbon.controller;

import com.lowcarbon.common.Result;
import com.lowcarbon.dto.MapPoiDTO;
import com.lowcarbon.entity.MapPoi;
import com.lowcarbon.service.MapManageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 地图管理Controller（管理员功能）
 */
@Tag(name = "地图管理", description = "管理员地图管理相关接口")
@RestController
@RequestMapping("/map-manage")
public class MapManageController {
    
    @Autowired
    private MapManageService mapManageService;
    
    /**
     * 获取所有用户的足迹点（管理员查看）
     */
    @GetMapping("/footprints/all")
    @Operation(summary = "获取所有用户足迹点（管理员）")
    public Result<List<Map<String, Object>>> getAllUsersFootprints(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (startDate == null && endDate == null) {
            endDate = LocalDate.now();
            startDate = endDate.minusDays(6);
        }
        
        List<Map<String, Object>> footprints = mapManageService.getAllUsersFootprints(startDate, endDate);
        return Result.success(footprints);
    }
    
    /**
     * 获取所有预设地点
     */
    @GetMapping("/pois")
    @Operation(summary = "获取所有预设地点")
    public Result<List<MapPoi>> getAllPois() {
        List<MapPoi> pois = mapManageService.getAllPois();
        return Result.success(pois);
    }
    
    /**
     * 创建预设地点
     */
    @PostMapping("/pois")
    @Operation(summary = "创建预设地点")
    public Result<MapPoi> createPoi(@Valid @RequestBody MapPoiDTO dto) {
        MapPoi poi = mapManageService.createPoi(dto);
        return Result.success(poi);
    }
    
    /**
     * 更新预设地点
     */
    @PutMapping("/pois/{id}")
    @Operation(summary = "更新预设地点")
    public Result<MapPoi> updatePoi(@PathVariable Long id, @Valid @RequestBody MapPoiDTO dto) {
        MapPoi poi = mapManageService.updatePoi(id, dto);
        return Result.success(poi);
    }
    
    /**
     * 删除预设地点
     */
    @DeleteMapping("/pois/{id}")
    @Operation(summary = "删除预设地点")
    public Result<Void> deletePoi(@PathVariable Long id) {
        mapManageService.deletePoi(id);
        return Result.success();
    }
}
