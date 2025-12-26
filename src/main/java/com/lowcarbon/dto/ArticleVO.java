package com.lowcarbon.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ArticleVO {
    private Long id;
    private String title;
    private String coverImg;
    private String content;
    private String category;
    private Integer views;
    private Long authorId;
    private String authorName;
    private Integer isTop;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

