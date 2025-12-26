package com.lowcarbon.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AchievementVO {
    private String title;
    private String description;
    private String iconUrl;
    private LocalDateTime createTime;
}

