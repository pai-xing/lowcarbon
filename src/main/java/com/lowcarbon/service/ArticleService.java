package com.lowcarbon.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lowcarbon.dto.ArticleCreateDTO;
import com.lowcarbon.dto.ArticleQueryDTO;
import com.lowcarbon.dto.ArticleUpdateDTO;
import com.lowcarbon.dto.ArticleVO;
import com.lowcarbon.entity.Article;

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
}

