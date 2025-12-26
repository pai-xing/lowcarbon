package com.lowcarbon.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class UserInfoVO {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String bio;
    private Integer points;
    private BigDecimal totalReduction;
    private List<AchievementVO> achievements; // 获得的勋章列表
}
