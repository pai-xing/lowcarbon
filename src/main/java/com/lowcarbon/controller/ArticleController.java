package com.lowcarbon.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lowcarbon.common.Result;
import com.lowcarbon.dto.ArticleCreateDTO;
import com.lowcarbon.dto.ArticleQueryDTO;
import com.lowcarbon.dto.ArticleUpdateDTO;
import com.lowcarbon.dto.ArticleVO;
import com.lowcarbon.dto.CommentVO;
import com.lowcarbon.service.ArticleService;
import com.lowcarbon.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "科普内容管理模块", description = "文章展示、详情、后台管理相关接口")
@RestController
@RequestMapping("/article")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private JwtUtil jwtUtil;

    @Operation(summary = "文章列表", description = "游客无需登录即可浏览，支持分类和关键词搜索")
    @GetMapping("/list")
    public Result<IPage<ArticleVO>> getArticleList(ArticleQueryDTO queryDTO) {
        try {
            IPage<ArticleVO> page = articleService.getArticleList(queryDTO);
            return Result.success(page);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "文章详情", description = "查看文章详情，自动记录浏览量")
    @GetMapping("/{id}")
    public Result<ArticleVO> getArticleDetail(@PathVariable Long id) {
        try {
            ArticleVO article = articleService.getArticleDetail(id);
            return Result.success(article);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "创建文章", description = "管理员发布文章")
    @PostMapping
    public Result<Void> createArticle(@Valid @RequestBody ArticleCreateDTO createDTO, HttpServletRequest request) {
        try {
            String token = getTokenFromRequest(request);
            Long authorId = jwtUtil.getUserIdFromToken(token);
            articleService.createArticle(createDTO, authorId);
            return Result.success("发布成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "更新文章", description = "管理员编辑文章")
    @PutMapping("/{id}")
    public Result<Void> updateArticle(@PathVariable Long id, @RequestBody ArticleUpdateDTO updateDTO) {
        try {
            articleService.updateArticle(id, updateDTO);
            return Result.success("更新成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "删除文章", description = "管理员删除文章")
    @DeleteMapping("/{id}")
    public Result<Void> deleteArticle(@PathVariable Long id) {
        try {
            articleService.deleteArticle(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "置顶/取消置顶", description = "管理员置顶或取消置顶文章")
    @PutMapping("/{id}/top")
    public Result<Void> toggleTop(@PathVariable Long id) {
        try {
            articleService.toggleTop(id);
            return Result.success("操作成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "点赞文章", description = "用户点赞文章")
    @PostMapping("/{id}/like")
    public Result<Void> likeArticle(@PathVariable Long id, HttpServletRequest request) {
        try {
            String token = getTokenFromRequest(request);
            Long userId = jwtUtil.getUserIdFromToken(token);
            articleService.likeArticle(id, userId);
            return Result.success("点赞成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "取消点赞", description = "用户取消点赞文章")
    @DeleteMapping("/{id}/like")
    public Result<Void> unlikeArticle(@PathVariable Long id, HttpServletRequest request) {
        try {
            String token = getTokenFromRequest(request);
            Long userId = jwtUtil.getUserIdFromToken(token);
            articleService.unlikeArticle(id, userId);
            return Result.success("已取消点赞");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "检查点赞状态", description = "检查用户是否已点赞该文章")
    @GetMapping("/{id}/like/status")
    public Result<Map<String, Boolean>> checkLikeStatus(@PathVariable Long id, HttpServletRequest request) {
        try {
            String token = getTokenFromRequest(request);
            Long userId = jwtUtil.getUserIdFromToken(token);
            boolean hasLiked = articleService.hasLiked(id, userId);
            Map<String, Boolean> result = new HashMap<>();
            result.put("hasLiked", hasLiked);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "收藏文章", description = "用户收藏文章")
    @PostMapping("/{id}/favorite")
    public Result<Void> favoriteArticle(@PathVariable Long id, HttpServletRequest request) {
        try {
            String token = getTokenFromRequest(request);
            Long userId = jwtUtil.getUserIdFromToken(token);
            articleService.favoriteArticle(id, userId);
            return Result.success("收藏成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "取消收藏", description = "用户取消收藏文章")
    @DeleteMapping("/{id}/favorite")
    public Result<Void> unfavoriteArticle(@PathVariable Long id, HttpServletRequest request) {
        try {
            String token = getTokenFromRequest(request);
            Long userId = jwtUtil.getUserIdFromToken(token);
            articleService.unfavoriteArticle(id, userId);
            return Result.success("已取消收藏");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "检查收藏状态", description = "检查用户是否已收藏该文章")
    @GetMapping("/{id}/favorite/status")
    public Result<Map<String, Boolean>> checkFavoriteStatus(@PathVariable Long id, HttpServletRequest request) {
        try {
            String token = getTokenFromRequest(request);
            Long userId = jwtUtil.getUserIdFromToken(token);
            boolean hasFavorited = articleService.hasFavorited(id, userId);
            Map<String, Boolean> result = new HashMap<>();
            result.put("hasFavorited", hasFavorited);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "获取收藏列表", description = "获取用户收藏的文章列表")
    @GetMapping("/favorites")
    public Result<IPage<ArticleVO>> getFavoriteArticles(@RequestParam(defaultValue = "1") Integer pageNum,
                                                         @RequestParam(defaultValue = "10") Integer pageSize,
                                                         HttpServletRequest request) {
        try {
            String token = getTokenFromRequest(request);
            Long userId = jwtUtil.getUserIdFromToken(token);
            IPage<ArticleVO> page = articleService.getFavoriteArticles(userId, pageNum, pageSize);
            return Result.success(page);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "添加评论", description = "用户对文章发表评论")
    @PostMapping("/{id}/comment")
    public Result<Void> addComment(@PathVariable Long id, 
                                    @RequestBody Map<String, String> requestBody,
                                    HttpServletRequest request) {
        try {
            String token = getTokenFromRequest(request);
            Long userId = jwtUtil.getUserIdFromToken(token);
            String content = requestBody.get("content");
            if (content == null || content.trim().isEmpty()) {
                return Result.error("评论内容不能为空");
            }
            articleService.addComment(id, userId, content);
            return Result.success("评论成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "获取评论列表", description = "获取文章的所有评论")
    @GetMapping("/{id}/comments")
    public Result<List<CommentVO>> getComments(@PathVariable Long id) {
        try {
            List<CommentVO> comments = articleService.getComments(id);
            return Result.success(comments);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "删除评论", description = "用户删除自己的评论或管理员删除任意评论")
    @DeleteMapping("/comment/{commentId}")
    public Result<Void> deleteComment(@PathVariable Long commentId, HttpServletRequest request) {
        try {
            String token = getTokenFromRequest(request);
            Long userId = jwtUtil.getUserIdFromToken(token);
            articleService.deleteComment(commentId, userId);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new RuntimeException("未登录或Token无效");
    }
}
