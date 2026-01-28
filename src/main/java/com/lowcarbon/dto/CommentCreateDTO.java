package com.lowcarbon.dto;

import lombok.Data;

/**
 * 评论创建DTO
 */
@Data
public class CommentCreateDTO {
    
    /**
     * 帖子ID
     */
    private Long postId;
    
    /**
     * 目标用户ID（用于回复评论，可选）
     */
    private Long targetUserId;
    
    /**
     * 评论内容
     */
    private String content;
}
