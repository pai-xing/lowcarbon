package com.lowcarbon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lowcarbon.dto.ArticleVO;
import com.lowcarbon.entity.ArticleFavorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ArticleFavoriteMapper extends BaseMapper<ArticleFavorite> {
    
    @Select("SELECT a.*, u.username as authorName, u.nickname as authorNickname " +
            "FROM tb_article_favorite f " +
            "LEFT JOIN tb_article a ON f.article_id = a.id " +
            "LEFT JOIN tb_user u ON a.author_id = u.id " +
            "WHERE f.user_id = #{userId} " +
            "ORDER BY f.create_time DESC")
    List<ArticleVO> selectFavoriteArticlesByUserId(Long userId);
}
