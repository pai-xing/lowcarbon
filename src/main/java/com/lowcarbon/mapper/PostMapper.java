package com.lowcarbon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lowcarbon.dto.PostVO;
import com.lowcarbon.entity.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 帖子Mapper接口
 */
@Mapper
public interface PostMapper extends BaseMapper<Post> {
    
    /**
     * 查询帖子列表（关联用户信息和当前用户点赞状态）
     * 
     * @param status 帖子状态
     * @param currentUserId 当前用户ID（用于判断点赞状态）
     * @return 帖子列表
     */
    @Select("SELECT p.*, u.nickname, u.avatar, " +
            "EXISTS(SELECT 1 FROM tb_post_like pl WHERE pl.post_id = p.id AND pl.user_id = #{currentUserId}) as is_liked " +
            "FROM tb_post p " +
            "LEFT JOIN tb_user u ON p.user_id = u.id " +
            "WHERE p.status = #{status} " +
            "ORDER BY p.create_time DESC")
    List<PostVO> selectPostList(@Param("status") Integer status, @Param("currentUserId") Long currentUserId);
    
    /**
     * 根据ID查询帖子详情（关联用户信息和当前用户点赞状态）
     * 
     * @param postId 帖子ID
     * @param currentUserId 当前用户ID
     * @return 帖子详情
     */
    @Select("SELECT p.*, u.nickname, u.avatar, " +
            "EXISTS(SELECT 1 FROM tb_post_like pl WHERE pl.post_id = p.id AND pl.user_id = #{currentUserId}) as is_liked " +
            "FROM tb_post p " +
            "LEFT JOIN tb_user u ON p.user_id = u.id " +
            "WHERE p.id = #{postId}")
    PostVO selectPostById(@Param("postId") Long postId, @Param("currentUserId") Long currentUserId);
    
    /**
     * 查询用户发布的帖子列表
     * 
     * @param userId 用户ID
     * @param currentUserId 当前用户ID
     * @return 帖子列表
     */
    @Select("SELECT p.*, u.nickname, u.avatar, " +
            "EXISTS(SELECT 1 FROM tb_post_like pl WHERE pl.post_id = p.id AND pl.user_id = #{currentUserId}) as is_liked " +
            "FROM tb_post p " +
            "LEFT JOIN tb_user u ON p.user_id = u.id " +
            "WHERE p.user_id = #{userId} AND p.status != 2 " +
            "ORDER BY p.create_time DESC")
    List<PostVO> selectUserPosts(@Param("userId") Long userId, @Param("currentUserId") Long currentUserId);
}
