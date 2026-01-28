package com.lowcarbon.controller;

import com.lowcarbon.common.Result;
import com.lowcarbon.dto.ActivityVO;
import com.lowcarbon.dto.RankingVO;
import com.lowcarbon.entity.Activity;
import com.lowcarbon.service.ActivityService;
import com.lowcarbon.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 活动控制器
 */
@Tag(name = "活动管理")
@RestController
@RequestMapping("/api/activity")
@RequiredArgsConstructor
public class ActivityController {
    
    private final ActivityService activityService;
    
    @Operation(summary = "创建活动(管理员)")
    @PostMapping("/create")
    public Result<Void> createActivity(@RequestBody Activity activity, @RequestHeader("Authorization") String token) {
        // 简化实现：暂不验证管理员权限
        activityService.createActivity(activity);
        return Result.success();
    }
    
    @Operation(summary = "更新活动(管理员)")
    @PutMapping("/update")
    public Result<Void> updateActivity(@RequestBody Activity activity, @RequestHeader("Authorization") String token) {
        // 简化实现：暂不验证管理员权限
        activityService.updateActivity(activity);
        return Result.success();
    }
    
    @Operation(summary = "删除活动(管理员)")
    @DeleteMapping("/{id}")
    public Result<Void> deleteActivity(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        // 简化实现：暂不验证管理员权限
        activityService.deleteActivity(id);
        return Result.success();
    }
    
    @Operation(summary = "获取活动列表")
    @GetMapping("/list")
    public Result<List<ActivityVO>> getActivityList(
            @RequestParam(required = false) Integer status,
            @RequestHeader(value = "Authorization", required = false) String token) {
        
        Long userId = null;
        if (token != null && !token.isEmpty()) {
            userId = JwtUtil.getUserIdFromToken(token);
        }
        
        List<ActivityVO> list = activityService.getActivityList(status, userId);
        return Result.success(list);
    }
    
    @Operation(summary = "获取活动详情")
    @GetMapping("/{id}")
    public Result<ActivityVO> getActivityDetail(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String token) {
        
        Long userId = null;
        if (token != null && !token.isEmpty()) {
            userId = JwtUtil.getUserIdFromToken(token);
        }
        
        ActivityVO detail = activityService.getActivityDetail(id, userId);
        return Result.success(detail);
    }
    
    @Operation(summary = "参加活动")
    @PostMapping("/join/{activityId}")
    public Result<Void> joinActivity(@PathVariable Long activityId, @RequestHeader("Authorization") String token) {
        Long userId = JwtUtil.getUserIdFromToken(token);
        activityService.joinActivity(activityId, userId);
        return Result.success();
    }
    
    @Operation(summary = "获取活动排行榜")
    @GetMapping("/ranking/{activityId}")
    public Result<List<RankingVO>> getActivityRanking(
            @PathVariable Long activityId,
            @RequestParam(defaultValue = "100") Integer limit) {
        
        List<RankingVO> rankings = activityService.getActivityRanking(activityId, limit);
        return Result.success(rankings);
    }
    
    @Operation(summary = "获取我的活动排名")
    @GetMapping("/my-ranking/{activityId}")
    public Result<RankingVO> getMyRanking(@PathVariable Long activityId, @RequestHeader("Authorization") String token) {
        Long userId = JwtUtil.getUserIdFromToken(token);
        RankingVO ranking = activityService.getUserRanking(activityId, userId);
        return Result.success(ranking);
    }
}
