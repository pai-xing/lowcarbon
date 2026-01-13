package com.lowcarbon.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lowcarbon.common.Result;
import com.lowcarbon.dto.FootprintCreateDTO;
import com.lowcarbon.dto.FootprintVO;
import com.lowcarbon.service.FootprintService;
import com.lowcarbon.service.impl.FootprintServiceImpl;
import com.lowcarbon.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Tag(name = "碳足迹管理", description = "个人碳足迹记录和统计相关接口")
@RestController
@RequestMapping("/footprint")
public class FootprintController {
    
    @Autowired
    private FootprintService footprintService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 获取所有行为类型配置
     */
    @GetMapping("/behavior-types")
    @Operation(summary = "获取所有行为类型配置")
    public Result<Map<String, Map<String, Object>>> getBehaviorTypes() {
        return Result.success(FootprintServiceImpl.getBehaviorTypes());
    }
    
    /**
     * 创建碳足迹记录
     */
    @PostMapping
    @Operation(summary = "创建碳足迹记录")
    public Result<FootprintVO> createFootprint(@Valid @RequestBody FootprintCreateDTO createDTO,
                                               HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        Long userId = jwtUtil.getUserIdFromToken(token);
        FootprintVO footprintVO = footprintService.createFootprint(createDTO, userId);
        return Result.success(footprintVO);
    }
    
    /**
     * 获取碳足迹记录列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取碳足迹记录列表")
    public Result<IPage<FootprintVO>> getFootprintList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        Long userId = jwtUtil.getUserIdFromToken(token);
        IPage<FootprintVO> page = footprintService.getFootprintList(userId, pageNum, pageSize, startDate, endDate);
        return Result.success(page);
    }
    
    /**
     * 获取碳足迹统计数据
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取碳足迹统计数据")
    public Result<Map<String, Object>> getStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        Long userId = jwtUtil.getUserIdFromToken(token);
        
        // 如果没有指定日期，默认查询最近30天
        if (startDate == null && endDate == null) {
            endDate = LocalDate.now();
            startDate = endDate.minusDays(30);
        }
        
        Map<String, Object> statistics = footprintService.getStatistics(userId, startDate, endDate);
        return Result.success(statistics);
    }
    
    /**
     * 获取按行为类型的统计
     */
    @GetMapping("/statistics/by-behavior")
    @Operation(summary = "获取按行为类型的统计")
    public Result<List<Map<String, Object>>> getStatisticsByBehaviorType(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        Long userId = jwtUtil.getUserIdFromToken(token);
        List<Map<String, Object>> statistics = footprintService.getStatisticsByBehaviorType(userId);
        return Result.success(statistics);
    }
    
    /**
     * 获取按日期的统计（用于趋势图）
     */
    @GetMapping("/statistics/daily")
    @Operation(summary = "获取按日期的统计")
    public Result<List<Map<String, Object>>> getDailyStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        Long userId = jwtUtil.getUserIdFromToken(token);
        
        // 如果没有指定日期，默认查询最近30天
        if (startDate == null && endDate == null) {
            endDate = LocalDate.now();
            startDate = endDate.minusDays(30);
        }
        
        List<Map<String, Object>> statistics = footprintService.getDailyStatistics(userId, startDate, endDate);
        return Result.success(statistics);
    }
    
    /**
     * 标准打卡（单位为“次”的行为）
     */
    @PostMapping("/checkin")
    @Operation(summary = "标准打卡（单位为“次”的行为，一天仅一次）")
    public Result<FootprintVO> checkin(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        Long userId = jwtUtil.getUserIdFromToken(token);
        String behaviorType = body.get("behaviorType");
        String remark = body.getOrDefault("remark", null);
        FootprintVO vo = footprintService.checkin(behaviorType, userId, remark);
        return Result.success(vo);
    }
    
    /**
     * 打卡日历（按月）
     */
    @GetMapping("/checkin/calendar")
    @Operation(summary = "获取指定月份打卡日历")
    public Result<List<Map<String, Object>>> getCheckinCalendar(
            @RequestParam String month,
            HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        Long userId = jwtUtil.getUserIdFromToken(token);
        LocalDate monthDate = LocalDate.parse(month + "-01");
        LocalDate monthStart = monthDate.withDayOfMonth(1);
        LocalDate monthEnd = monthDate.withDayOfMonth(monthDate.lengthOfMonth());
        List<Map<String, Object>> calendar = footprintService.getCheckinCalendar(userId, monthStart, monthEnd);
        return Result.success(calendar);
    }
    
    /**
     * 打卡统计（streak、totalCheckins、monthlyRate）
     */
    @GetMapping("/checkin/stats")
    @Operation(summary = "获取用户打卡统计数据")
    public Result<Map<String, Object>> getCheckinStats(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        Long userId = jwtUtil.getUserIdFromToken(token);
        Map<String, Object> stats = footprintService.getCheckinStats(userId);
        return Result.success(stats);
    }
    
    /**
     * 更新碳足迹记录
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新碳足迹记录")
    public Result<FootprintVO> updateFootprint(@PathVariable Long id,
                                               @Valid @RequestBody FootprintCreateDTO updateDTO,
                                               HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        Long userId = jwtUtil.getUserIdFromToken(token);
        FootprintVO vo = footprintService.updateFootprint(id, updateDTO, userId);
        return Result.success(vo);
    }
    
    /**
     * 删除碳足迹记录
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除碳足迹记录")
    public Result<Void> deleteFootprint(@PathVariable Long id, HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        Long userId = jwtUtil.getUserIdFromToken(token);
        footprintService.deleteFootprint(id, userId);
        return Result.success();
    }
    
    /**
     * 从请求中获取token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
