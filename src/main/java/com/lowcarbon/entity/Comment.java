package com.lowcarbon.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 社区评论实体类
 */
@Data
@TableName("tb_comment")
public class Comment {
    
    /**
     * 评论ID
     */
    @TableId(type = IdType.AUTO)
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
     * 目标用户ID（用于回复评论）
     */
    private Long targetUserId;
    
    /**
     * 评论内容
     */
    private String content;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
