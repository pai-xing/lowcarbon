package com.lowcarbon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lowcarbon.dto.PostCommentVO;
import com.lowcarbon.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 评论Mapper接口
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
    
    /**
     * 查询帖子的评论列表（关联用户信息）
     * 
     * @param postId 帖子ID
     * @return 评论列表
     */
    @Select("SELECT c.*, u1.nickname, u1.avatar, u2.nickname as target_nickname " +
            "FROM tb_comment c " +
            "LEFT JOIN tb_user u1 ON c.user_id = u1.id " +
            "LEFT JOIN tb_user u2 ON c.target_user_id = u2.id " +
            "WHERE c.post_id = #{postId} " +
            "ORDER BY c.create_time ASC")
    List<PostCommentVO> selectCommentsByPostId(@Param("postId") Long postId);
}
