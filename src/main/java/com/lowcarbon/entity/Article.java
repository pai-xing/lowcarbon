package com.lowcarbon.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_article")
public class Article {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String coverImg;
    private String content;
    private String category;
    private Integer views;
    private Integer likesCount;
    private Integer commentsCount;
    private Integer favoritesCount;
    private Long authorId;
    private Integer isTop;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
