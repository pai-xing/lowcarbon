package com.lowcarbon.controller;

import com.lowcarbon.common.Result;
import com.lowcarbon.dto.LeaderboardResponseVO;
import com.lowcarbon.service.LeaderboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 排行榜控制器
 */
@Tag(name = "排行榜管理")
@RestController
@RequestMapping("/api/leaderboard")
public class LeaderboardController {
    
    @Autowired
    private LeaderboardService leaderboardService;
    
    /**
     * 获取排行榜
     * @param type 榜单类型（total-总榜，week-周榜，month-月榜，activity-活动榜）
     * @param activityId 活动ID（仅type=activity时需要）
     * @param limit 返回条数，默认50
     * @param userId 当前登录用户ID
     * @return 排行榜数据
     */
    @Operation(summary = "获取排行榜")
    @GetMapping
    public Result<LeaderboardResponseVO> getLeaderboard(
            @Parameter(description = "榜单类型", required = true) @RequestParam(defaultValue = "total") String type,
            @Parameter(description = "活动ID") @RequestParam(required = false) Long activityId,
            @Parameter(description = "返回条数") @RequestParam(required = false, defaultValue = "50") Integer limit,
            @RequestAttribute(value = "userId", required = false) Long userId) {
        
        LeaderboardResponseVO response = leaderboardService.getLeaderboard(type, activityId, limit, userId);
        return Result.success(response);
    }
}
