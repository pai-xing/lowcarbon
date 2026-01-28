package com.lowcarbon.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 社区帖子实体类
 */
@Data
@TableName("tb_post")
public class Post {
    
    /**
     * 帖子ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 发布用户ID
     */
    private Long userId;
    
    /**
     * 帖子内容
     */
    private String content;
    
    /**
     * 图片URL列表（JSON格式存储）
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
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
