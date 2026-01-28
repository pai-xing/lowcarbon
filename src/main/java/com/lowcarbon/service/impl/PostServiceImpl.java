package com.lowcarbon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lowcarbon.dto.CommentCreateDTO;
import com.lowcarbon.dto.PostCommentVO;
import com.lowcarbon.dto.PostCreateDTO;
import com.lowcarbon.dto.PostVO;
import com.lowcarbon.entity.Comment;
import com.lowcarbon.entity.Post;
import com.lowcarbon.entity.PostLike;
import com.lowcarbon.mapper.CommentMapper;
import com.lowcarbon.mapper.PostLikeMapper;
import com.lowcarbon.mapper.PostMapper;
import com.lowcarbon.service.PostService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 帖子服务实现类
 */
@Service
public class PostServiceImpl implements PostService {
    
    @Autowired
    private PostMapper postMapper;
    
    @Autowired
    private CommentMapper commentMapper;
    
    @Autowired
    private PostLikeMapper postLikeMapper;
    
    @Override
    @Transactional
    public void createPost(PostCreateDTO dto, Long userId) {
        Post post = new Post();
        BeanUtils.copyProperties(dto, post);
        post.setUserId(userId);
        post.setLikesCount(0);
        post.setCommentsCount(0);
        post.setStatus(0); // 待审核
        post.setCreateTime(LocalDateTime.now());
        post.setUpdateTime(LocalDateTime.now());
        
        postMapper.insert(post);
    }
    
    @Override
    public IPage<PostVO> getPostList(Integer status, Integer page, Integer pageSize, Long currentUserId) {
        // 创建分页对象
        Page<Post> postPage = new Page<>(page, pageSize);
        
        // 构建查询条件
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Post::getStatus, status);
        }
        wrapper.orderByDesc(Post::getCreateTime);
        
        // 分页查询
        IPage<Post> result = postMapper.selectPage(postPage, wrapper);
        
        // 转换为VO并填充用户信息和点赞状态
        IPage<PostVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<PostVO> voList = result.getRecords().stream().map(post -> {
            // 使用Mapper的自定义查询方法获取完整的PostVO（包含用户信息和点赞状态）
            return postMapper.selectPostById(post.getId(), currentUserId);
        }).collect(Collectors.toList());
        
        voPage.setRecords(voList);
        return voPage;
    }
    
    @Override
    public PostVO getPostById(Long postId, Long currentUserId) {
        return postMapper.selectPostById(postId, currentUserId);
    }
    
    @Override
    public List<PostVO> getUserPosts(Long userId, Long currentUserId) {
        return postMapper.selectUserPosts(userId, currentUserId);
    }
    
    @Override
    @Transactional
    public void deletePost(Long postId, Long userId) {
        // 查询帖子
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new RuntimeException("帖子不存在");
        }
        
        // 验证权限（只能删除自己的帖子）
        if (!post.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除该帖子");
        }
        
        // 软删除：更新状态为已删除
        post.setStatus(2);
        post.setUpdateTime(LocalDateTime.now());
        postMapper.updateById(post);
    }
    
    @Override
    @Transactional
    public void toggleLike(Long postId, Long userId) {
        // 查询是否已点赞
        LambdaQueryWrapper<PostLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PostLike::getPostId, postId)
               .eq(PostLike::getUserId, userId);
        PostLike existingLike = postLikeMapper.selectOne(wrapper);
        
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new RuntimeException("帖子不存在");
        }
        
        if (existingLike != null) {
            // 已点赞，取消点赞
            postLikeMapper.deleteById(existingLike.getId());
            post.setLikesCount(Math.max(0, post.getLikesCount() - 1));
        } else {
            // 未点赞，添加点赞
            PostLike like = new PostLike();
            like.setPostId(postId);
            like.setUserId(userId);
            like.setCreateTime(LocalDateTime.now());
            postLikeMapper.insert(like);
            post.setLikesCount(post.getLikesCount() + 1);
        }
        
        post.setUpdateTime(LocalDateTime.now());
        postMapper.updateById(post);
    }
    
    @Override
    @Transactional
    public void addComment(CommentCreateDTO dto, Long userId) {
        Comment comment = new Comment();
        BeanUtils.copyProperties(dto, comment);
        comment.setUserId(userId);
        comment.setCreateTime(LocalDateTime.now());
        
        commentMapper.insert(comment);
        
        // 更新帖子评论数
        Post post = postMapper.selectById(dto.getPostId());
        if (post != null) {
            post.setCommentsCount(post.getCommentsCount() + 1);
            post.setUpdateTime(LocalDateTime.now());
            postMapper.updateById(post);
        }
    }
    
    @Override
    public List<PostCommentVO> getCommentsByPostId(Long postId) {
        return commentMapper.selectCommentsByPostId(postId);
    }
    
    @Override
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        // 查询评论
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }
        
        // 验证权限（只能删除自己的评论）
        if (!comment.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除该评论");
        }
        
        // 删除评论
        commentMapper.deleteById(commentId);
        
        // 更新帖子评论数
        Post post = postMapper.selectById(comment.getPostId());
        if (post != null) {
            post.setCommentsCount(Math.max(0, post.getCommentsCount() - 1));
            post.setUpdateTime(LocalDateTime.now());
            postMapper.updateById(post);
        }
    }
    
    @Override
    @Transactional
    public void auditPost(Long postId, Integer status) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new RuntimeException("帖子不存在");
        }
        
        post.setStatus(status);
        post.setUpdateTime(LocalDateTime.now());
        postMapper.updateById(post);
    }
}
