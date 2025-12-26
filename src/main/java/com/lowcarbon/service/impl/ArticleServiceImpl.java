package com.lowcarbon.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lowcarbon.dto.ArticleCreateDTO;
import com.lowcarbon.dto.ArticleQueryDTO;
import com.lowcarbon.dto.ArticleUpdateDTO;
import com.lowcarbon.dto.ArticleVO;
import com.lowcarbon.entity.Article;
import com.lowcarbon.entity.User;
import com.lowcarbon.mapper.ArticleMapper;
import com.lowcarbon.mapper.UserMapper;
import com.lowcarbon.service.ArticleService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public IPage<ArticleVO> getArticleList(ArticleQueryDTO queryDTO) {
        Page<Article> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();

        // 关键词搜索
        if (StrUtil.isNotBlank(queryDTO.getKeyword())) {
            wrapper.and(w -> w.like(Article::getTitle, queryDTO.getKeyword())
                    .or()
                    .like(Article::getContent, queryDTO.getKeyword()));
        }

        // 分类筛选
        if (StrUtil.isNotBlank(queryDTO.getCategory())) {
            wrapper.eq(Article::getCategory, queryDTO.getCategory());
        }

        // 排序：置顶优先，然后按创建时间倒序
        wrapper.orderByDesc(Article::getIsTop)
               .orderByDesc(Article::getCreateTime);

        IPage<Article> articlePage = articleMapper.selectPage(page, wrapper);

        // 转换为VO
        IPage<ArticleVO> voPage = articlePage.convert(article -> {
            ArticleVO vo = new ArticleVO();
            BeanUtils.copyProperties(article, vo);

            // 查询作者信息
            if (article.getAuthorId() != null) {
                User author = userMapper.selectById(article.getAuthorId());
                if (author != null) {
                    vo.setAuthorName(author.getNickname());
                }
            }

            return vo;
        });

        return voPage;
    }

    @Override
    @Transactional
    public ArticleVO getArticleDetail(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new RuntimeException("文章不存在");
        }

        // 增加浏览量
        articleMapper.incrementViews(id);

        // 重新查询以获取最新的浏览量
        article = articleMapper.selectById(id);

        ArticleVO vo = new ArticleVO();
        BeanUtils.copyProperties(article, vo);

        // 查询作者信息
        if (article.getAuthorId() != null) {
            User author = userMapper.selectById(article.getAuthorId());
            if (author != null) {
                vo.setAuthorName(author.getNickname());
            }
        }

        return vo;
    }

    @Override
    @Transactional
    public void createArticle(ArticleCreateDTO createDTO, Long authorId) {
        Article article = new Article();
        article.setTitle(createDTO.getTitle());
        article.setCoverImg(createDTO.getCoverImg());
        article.setContent(createDTO.getContent());
        article.setCategory(createDTO.getCategory());
        article.setAuthorId(authorId);
        article.setViews(0);
        article.setIsTop(createDTO.getIsTop() != null && createDTO.getIsTop() == 1 ? 1 : 0);

        articleMapper.insert(article);
    }

    @Override
    @Transactional
    public void updateArticle(Long id, ArticleUpdateDTO updateDTO) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new RuntimeException("文章不存在");
        }

        if (StrUtil.isNotBlank(updateDTO.getTitle())) {
            article.setTitle(updateDTO.getTitle());
        }
        if (updateDTO.getCoverImg() != null) {
            article.setCoverImg(updateDTO.getCoverImg());
        }
        if (StrUtil.isNotBlank(updateDTO.getContent())) {
            article.setContent(updateDTO.getContent());
        }
        if (StrUtil.isNotBlank(updateDTO.getCategory())) {
            article.setCategory(updateDTO.getCategory());
        }
        if (updateDTO.getIsTop() != null) {
            article.setIsTop(updateDTO.getIsTop());
        }

        articleMapper.updateById(article);
    }

    @Override
    @Transactional
    public void deleteArticle(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new RuntimeException("文章不存在");
        }
        articleMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void toggleTop(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new RuntimeException("文章不存在");
        }
        article.setIsTop(article.getIsTop() == 1 ? 0 : 1);
        articleMapper.updateById(article);
    }
}

