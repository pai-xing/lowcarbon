package com.lowcarbon.dto;

import lombok.Data;

/**
 * 帖子创建DTO
 */
@Data
public class PostCreateDTO {
    
    /**
     * 帖子内容
     */
    private String content;
    
    /**
     * 图片URL列表（JSON格式）
     */
    private String images;
}
