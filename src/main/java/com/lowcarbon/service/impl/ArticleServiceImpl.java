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
import com.lowcarbon.dto.CommentVO;
import com.lowcarbon.entity.Article;
import com.lowcarbon.entity.ArticleComment;
import com.lowcarbon.entity.ArticleFavorite;
import com.lowcarbon.entity.ArticleLike;
import com.lowcarbon.entity.User;
import com.lowcarbon.mapper.ArticleCommentMapper;
import com.lowcarbon.mapper.ArticleFavoriteMapper;
import com.lowcarbon.mapper.ArticleLikeMapper;
import com.lowcarbon.mapper.ArticleMapper;
import com.lowcarbon.mapper.UserMapper;
import com.lowcarbon.service.ArticleService;

import java.util.List;
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

    @Autowired
    private ArticleLikeMapper articleLikeMapper;

    @Autowired
    private ArticleFavoriteMapper articleFavoriteMapper;

    @Autowired
    private ArticleCommentMapper articleCommentMapper;

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

        // 作者筛选
        if (queryDTO.getAuthorId() != null) {
            wrapper.eq(Article::getAuthorId, queryDTO.getAuthorId());
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

    @Override
    @Transactional
    public void likeArticle(Long articleId, Long userId) {
        // 检查是否已点赞
        LambdaQueryWrapper<ArticleLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArticleLike::getArticleId, articleId)
               .eq(ArticleLike::getUserId, userId);
        Long count = articleLikeMapper.selectCount(wrapper);
        
        if (count > 0) {
            throw new RuntimeException("已经点赞过了");
        }

        // 添加点赞记录
        ArticleLike like = new ArticleLike();
        like.setArticleId(articleId);
        like.setUserId(userId);
        articleLikeMapper.insert(like);

        // 更新文章点赞数
        Article article = articleMapper.selectById(articleId);
        if (article != null) {
            article.setLikesCount(article.getLikesCount() == null ? 1 : article.getLikesCount() + 1);
            articleMapper.updateById(article);
        }
    }

    @Override
    @Transactional
    public void unlikeArticle(Long articleId, Long userId) {
        // 删除点赞记录
        LambdaQueryWrapper<ArticleLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArticleLike::getArticleId, articleId)
               .eq(ArticleLike::getUserId, userId);
        articleLikeMapper.delete(wrapper);

        // 更新文章点赞数
        Article article = articleMapper.selectById(articleId);
        if (article != null && article.getLikesCount() != null && article.getLikesCount() > 0) {
            article.setLikesCount(article.getLikesCount() - 1);
            articleMapper.updateById(article);
        }
    }

    @Override
    public boolean hasLiked(Long articleId, Long userId) {
        LambdaQueryWrapper<ArticleLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArticleLike::getArticleId, articleId)
               .eq(ArticleLike::getUserId, userId);
        return articleLikeMapper.selectCount(wrapper) > 0;
    }

    @Override
    @Transactional
    public void favoriteArticle(Long articleId, Long userId) {
        // 检查是否已收藏
        LambdaQueryWrapper<ArticleFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArticleFavorite::getArticleId, articleId)
               .eq(ArticleFavorite::getUserId, userId);
        Long count = articleFavoriteMapper.selectCount(wrapper);
        
        if (count > 0) {
            throw new RuntimeException("已经收藏过了");
        }

        // 添加收藏记录
        ArticleFavorite favorite = new ArticleFavorite();
        favorite.setArticleId(articleId);
        favorite.setUserId(userId);
        articleFavoriteMapper.insert(favorite);

        // 更新文章收藏数
        Article article = articleMapper.selectById(articleId);
        if (article != null) {
            article.setFavoritesCount(article.getFavoritesCount() == null ? 1 : article.getFavoritesCount() + 1);
            articleMapper.updateById(article);
        }
    }

    @Override
    @Transactional
    public void unfavoriteArticle(Long articleId, Long userId) {
        // 删除收藏记录
        LambdaQueryWrapper<ArticleFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArticleFavorite::getArticleId, articleId)
               .eq(ArticleFavorite::getUserId, userId);
        articleFavoriteMapper.delete(wrapper);

        // 更新文章收藏数
        Article article = articleMapper.selectById(articleId);
        if (article != null && article.getFavoritesCount() != null && article.getFavoritesCount() > 0) {
            article.setFavoritesCount(article.getFavoritesCount() - 1);
            articleMapper.updateById(article);
        }
    }

    @Override
    public boolean hasFavorited(Long articleId, Long userId) {
        LambdaQueryWrapper<ArticleFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArticleFavorite::getArticleId, articleId)
               .eq(ArticleFavorite::getUserId, userId);
        return articleFavoriteMapper.selectCount(wrapper) > 0;
    }

    @Override
    public IPage<ArticleVO> getFavoriteArticles(Long userId, Integer pageNum, Integer pageSize) {
        // 创建分页对象
        Page<ArticleVO> page = new Page<>(pageNum, pageSize);
        
        // 获取用户收藏的文章列表（分页）
        List<ArticleVO> favoriteArticles = articleFavoriteMapper.selectFavoriteArticlesByUserId(userId);
        
        // 手动分页处理
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, favoriteArticles.size());
        
        List<ArticleVO> pageRecords = favoriteArticles.subList(
            Math.min(start, favoriteArticles.size()), 
            Math.min(end, favoriteArticles.size())
        );
        
        page.setRecords(pageRecords);
        page.setTotal(favoriteArticles.size());
        page.setCurrent(pageNum);
        page.setSize(pageSize);
        
        return page;
    }

    @Override
    @Transactional
    public void addComment(Long articleId, Long userId, String content) {
        // 添加评论记录
        ArticleComment comment = new ArticleComment();
        comment.setArticleId(articleId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setParentId(null); // 暂不支持回复功能，统一设置为null
        articleCommentMapper.insert(comment);

        // 更新文章评论数
        Article article = articleMapper.selectById(articleId);
        if (article != null) {
            article.setCommentsCount(article.getCommentsCount() == null ? 1 : article.getCommentsCount() + 1);
            articleMapper.updateById(article);
        }
    }

    @Override
    public List<CommentVO> getComments(Long articleId) {
        return articleCommentMapper.selectCommentsByArticleId(articleId);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        ArticleComment comment = articleCommentMapper.selectById(commentId);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }

        // 只能删除自己的评论
        if (!comment.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除此评论");
        }

        articleCommentMapper.deleteById(commentId);

        // 更新文章评论数
        Article article = articleMapper.selectById(comment.getArticleId());
        if (article != null && article.getCommentsCount() != null && article.getCommentsCount() > 0) {
            article.setCommentsCount(article.getCommentsCount() - 1);
            articleMapper.updateById(article);
        }
    }
}
