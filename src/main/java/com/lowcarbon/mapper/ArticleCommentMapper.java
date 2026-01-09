package com.lowcarbon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lowcarbon.dto.CommentVO;
import com.lowcarbon.entity.ArticleComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ArticleCommentMapper extends BaseMapper<ArticleComment> {
    
    @Select("SELECT c.*, u.username, u.nickname, u.avatar " +
            "FROM tb_article_comment c " +
            "LEFT JOIN tb_user u ON c.user_id = u.id " +
            "WHERE c.article_id = #{articleId} " +
            "ORDER BY c.create_time DESC")
    List<CommentVO> selectCommentsByArticleId(Long articleId);
}
