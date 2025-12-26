package com.lowcarbon.dto;

import lombok.Data;

@Data
public class ArticleQueryDTO {
    private String keyword; // 关键词搜索
    private String category; // 分类筛选
    private Integer current = 1; // 当前页
    private Integer size = 10; // 每页数量
}

