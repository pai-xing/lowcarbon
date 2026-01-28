package com.lowcarbon.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lowcarbon.dto.CommentCreateDTO;
import com.lowcarbon.dto.PostCommentVO;
import com.lowcarbon.dto.PostCreateDTO;
import com.lowcarbon.dto.PostVO;

import java.util.List;

/**
 * 帖子服务接口
 */
public interface PostService {
    
    /**
     * 创建帖子
     * 
     * @param dto 帖子创建DTO
     * @param userId 用户ID
     */
    void createPost(PostCreateDTO dto, Long userId);
    
    /**
     * 查询帖子列表（支持分页）
     * 
     * @param status 帖子状态（0-待审核，1-已发布，null-全部）
     * @param page 页码
     * @param pageSize 每页数量
     * @param currentUserId 当前用户ID
     * @return 帖子分页列表
     */
    IPage<PostVO> getPostList(Integer status, Integer page, Integer pageSize, Long currentUserId);
    
    /**
     * 根据ID查询帖子详情
     * 
     * @param postId 帖子ID
     * @param currentUserId 当前用户ID
     * @return 帖子详情
     */
    PostVO getPostById(Long postId, Long currentUserId);
    
    /**
     * 查询用户发布的帖子列表
     * 
     * @param userId 用户ID
     * @param currentUserId 当前用户ID
     * @return 帖子列表
     */
    List<PostVO> getUserPosts(Long userId, Long currentUserId);
    
    /**
     * 删除帖子
     * 
     * @param postId 帖子ID
     * @param userId 用户ID
     */
    void deletePost(Long postId, Long userId);
    
    /**
     * 点赞/取消点赞帖子
     * 
     * @param postId 帖子ID
     * @param userId 用户ID
     */
    void toggleLike(Long postId, Long userId);
    
    /**
     * 添加评论
     * 
     * @param dto 评论创建DTO
     * @param userId 用户ID
     */
    void addComment(CommentCreateDTO dto, Long userId);
    
    /**
     * 查询帖子的评论列表
     * 
     * @param postId 帖子ID
     * @return 评论列表
     */
    List<PostCommentVO> getCommentsByPostId(Long postId);
    
    /**
     * 删除评论
     * 
     * @param commentId 评论ID
     * @param userId 用户ID
     */
    void deleteComment(Long commentId, Long userId);
    
    /**
     * 审核帖子（管理员）
     * 
     * @param postId 帖子ID
     * @param status 状态（1-通过，2-拒绝）
     */
    void auditPost(Long postId, Integer status);
}
