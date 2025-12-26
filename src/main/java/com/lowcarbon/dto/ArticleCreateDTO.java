package com.lowcarbon.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ArticleCreateDTO {
    @NotBlank(message = "文章标题不能为空")
    private String title;

    private String coverImg;
    
    @NotBlank(message = "文章内容不能为空")
    private String content;

    private String category;

    private Integer isTop;
}

