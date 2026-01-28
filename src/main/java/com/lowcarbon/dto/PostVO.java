package com.lowcarbon.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 帖子展示VO
 */
@Data
public class PostVO {
    
    /**
     * 帖子ID
     */
    private Long id;
    
    /**
     * 发布用户ID
     */
    private Long userId;
    
    /**
     * 用户昵称
     */
    private String nickname;
    
    /**
     * 用户头像
     */
    private String avatar;
    
    /**
     * 帖子内容
     */
    private String content;
    
    /**
     * 图片URL列表（JSON格式）
     */
    private String images;
    
    /**
     * 点赞数
     */
    private Integer likesCount;
    
    /**
     * 评论数
     */
    private Integer commentsCount;
    
    /**
     * 帖子状态：0-待审核，1-已发布，2-已删除
     */
    private Integer status;
    
    /**
     * 当前用户是否已点赞
     */
    private Boolean isLiked;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
