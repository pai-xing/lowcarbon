package com.lowcarbon.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 帖子评论展示VO
 */
@Data
public class PostCommentVO {
    
    /**
     * 评论ID
     */
    private Long id;
    
    /**
     * 帖子ID
     */
    private Long postId;
    
    /**
     * 评论用户ID
     */
    private Long userId;
    
    /**
     * 评论用户昵称
     */
    private String nickname;
    
    /**
     * 评论用户头像
     */
    private String avatar;
    
    /**
     * 目标用户ID（用于回复评论）
     */
    private Long targetUserId;
    
    /**
     * 目标用户昵称
     */
    private String targetNickname;
    
    /**
     * 评论内容
     */
    private String content;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
