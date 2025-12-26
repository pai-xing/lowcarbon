package com.lowcarbon.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lowcarbon.common.Result;
import com.lowcarbon.dto.ArticleCreateDTO;
import com.lowcarbon.dto.ArticleQueryDTO;
import com.lowcarbon.dto.ArticleUpdateDTO;
import com.lowcarbon.dto.ArticleVO;
import com.lowcarbon.service.ArticleService;
import com.lowcarbon.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    private String getTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new RuntimeException("未登录或Token无效");
    }
}

