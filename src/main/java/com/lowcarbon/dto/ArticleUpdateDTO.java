package com.lowcarbon.dto;

import lombok.Data;

@Data
public class ArticleUpdateDTO {
    private String title;
    private String coverImg;
    private String content;
    private String category;
    private Integer isTop;
}

