package com.lowcarbon.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lowcarbon.dto.ArticleCreateDTO;
import com.lowcarbon.dto.ArticleQueryDTO;
import com.lowcarbon.dto.ArticleUpdateDTO;
import com.lowcarbon.dto.ArticleVO;
import com.lowcarbon.dto.CommentVO;
import com.lowcarbon.entity.Article;

import java.util.List;

public interface ArticleService extends IService<Article> {
    /**
     * 分页查询文章列表（游客可访问）
     */
    IPage<ArticleVO> getArticleList(ArticleQueryDTO queryDTO);

    /**
     * 获取文章详情（自动增加浏览量）
     */
    ArticleVO getArticleDetail(Long id);

    /**
     * 创建文章（管理员）
     */
    void createArticle(ArticleCreateDTO createDTO, Long authorId);

    /**
     * 更新文章（管理员）
     */
    void updateArticle(Long id, ArticleUpdateDTO updateDTO);

    /**
     * 删除文章（管理员）
     */
    void deleteArticle(Long id);

    /**
     * 置顶/取消置顶文章（管理员）
     */
    void toggleTop(Long id);

    /**
     * 点赞文章
     */
    void likeArticle(Long articleId, Long userId);

    /**
     * 取消点赞文章
     */
    void unlikeArticle(Long articleId, Long userId);

    /**
     * 检查用户是否已点赞文章
     */
    boolean hasLiked(Long articleId, Long userId);

    /**
     * 收藏文章
     */
    void favoriteArticle(Long articleId, Long userId);

    /**
     * 取消收藏文章
     */
    void unfavoriteArticle(Long articleId, Long userId);

    /**
     * 检查用户是否已收藏文章
     */
    boolean hasFavorited(Long articleId, Long userId);

    /**
     * 获取用户收藏的文章列表（分页）
     */
    IPage<ArticleVO> getFavoriteArticles(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 添加评论
     */
    void addComment(Long articleId, Long userId, String content);

    /**
     * 获取文章的评论列表
     */
    List<CommentVO> getComments(Long articleId);

    /**
     * 删除评论
     */
    void deleteComment(Long commentId, Long userId);
}
