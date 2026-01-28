package com.lowcarbon.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lowcarbon.common.Result;
import com.lowcarbon.dto.CommentCreateDTO;
import com.lowcarbon.dto.PostCommentVO;
import com.lowcarbon.dto.PostCreateDTO;
import com.lowcarbon.dto.PostVO;
import com.lowcarbon.service.PostService;
import com.lowcarbon.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 帖子控制器
 */
@Tag(name = "帖子管理", description = "社区帖子相关接口")
@RestController
@RequestMapping("/posts")
public class PostController {
    
    @Autowired
    private PostService postService;
    
    /**
     * 创建帖子
     */
    @Operation(summary = "创建帖子", description = "用户发布新帖子")
    @PostMapping
    public Result<Void> createPost(@RequestBody PostCreateDTO dto,
                                    @RequestHeader("Authorization") String token) {
        Long userId = JwtUtil.getUserIdFromToken(token);
        postService.createPost(dto, userId);
        return Result.success();
    }
    
    /**
     * 获取帖子列表（支持分页）
     */
    @Operation(summary = "获取帖子列表", description = "根据审核状态获取帖子列表，支持分页")
    @GetMapping
    public Result<IPage<PostVO>> getPostList(@RequestParam(required = false) Integer status,
                                              @RequestParam(defaultValue = "1") Integer page,
                                              @RequestParam(defaultValue = "10") Integer pageSize,
                                              @RequestHeader(value = "Authorization", required = false) String token) {
        Long currentUserId = token != null ? JwtUtil.getUserIdFromToken(token) : null;
        IPage<PostVO> posts = postService.getPostList(status, page, pageSize, currentUserId);
        return Result.success(posts);
    }
    
    /**
     * 获取帖子详情
     */
    @Operation(summary = "获取帖子详情", description = "查看单个帖子的详细信息")
    @GetMapping("/{id}")
    public Result<PostVO> getPostById(@PathVariable Long id,
                                       @RequestHeader(value = "Authorization", required = false) String token) {
        Long currentUserId = token != null ? JwtUtil.getUserIdFromToken(token) : null;
        PostVO post = postService.getPostById(id, currentUserId);
        return Result.success(post);
    }
    
    /**
     * 获取用户发布的帖子列表
     */
    @Operation(summary = "获取用户发布的帖子列表", description = "查看指定用户发布的所有帖子")
    @GetMapping("/user/{userId}")
    public Result<List<PostVO>> getUserPosts(@PathVariable Long userId,
                                              @RequestHeader(value = "Authorization", required = false) String token) {
        Long currentUserId = token != null ? JwtUtil.getUserIdFromToken(token) : null;
        List<PostVO> posts = postService.getUserPosts(userId, currentUserId);
        return Result.success(posts);
    }
    
    /**
     * 删除帖子
     */
    @Operation(summary = "删除帖子", description = "用户删除自己发布的帖子")
    @DeleteMapping("/{id}")
    public Result<Void> deletePost(@PathVariable Long id,
                                    @RequestHeader("Authorization") String token) {
        Long userId = JwtUtil.getUserIdFromToken(token);
        postService.deletePost(id, userId);
        return Result.success();
    }
    
    /**
     * 点赞/取消点赞帖子
     */
    @Operation(summary = "点赞/取消点赞帖子", description = "切换帖子的点赞状态")
    @PostMapping("/{id}/like")
    public Result<Void> toggleLike(@PathVariable Long id,
                                    @RequestHeader("Authorization") String token) {
        Long userId = JwtUtil.getUserIdFromToken(token);
        postService.toggleLike(id, userId);
        return Result.success();
    }
    
    /**
     * 添加评论
     */
    @Operation(summary = "添加评论", description = "对帖子发表评论或回复其他评论")
    @PostMapping("/comments")
    public Result<Void> addComment(@RequestBody CommentCreateDTO dto,
                                    @RequestHeader("Authorization") String token) {
        Long userId = JwtUtil.getUserIdFromToken(token);
        postService.addComment(dto, userId);
        return Result.success();
    }
    
    /**
     * 获取帖子的评论列表
     */
    @Operation(summary = "获取帖子的评论列表", description = "查看帖子下的所有评论")
    @GetMapping("/{id}/comments")
    public Result<List<PostCommentVO>> getComments(@PathVariable Long id) {
        List<PostCommentVO> comments = postService.getCommentsByPostId(id);
        return Result.success(comments);
    }
    
    /**
     * 删除评论
     */
    @Operation(summary = "删除评论", description = "用户删除自己的评论")
    @DeleteMapping("/comments/{id}")
    public Result<Void> deleteComment(@PathVariable Long id,
                                       @RequestHeader("Authorization") String token) {
        Long userId = JwtUtil.getUserIdFromToken(token);
        postService.deleteComment(id, userId);
        return Result.success();
    }
    
    /**
     * 审核帖子（管理员）
     */
    @Operation(summary = "审核帖子", description = "管理员审核帖子，设置审核状态")
    @PutMapping("/{id}/audit")
    public Result<Void> auditPost(@PathVariable Long id,
                                   @RequestParam Integer status) {
        postService.auditPost(id, status);
        return Result.success();
    }
}
