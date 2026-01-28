package com.lowcarbon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lowcarbon.entity.PostLike;
import org.apache.ibatis.annotations.Mapper;

/**
 * 帖子点赞Mapper接口
 */
@Mapper
public interface PostLikeMapper extends BaseMapper<PostLike> {
}
