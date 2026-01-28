package com.lowcarbon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lowcarbon.entity.ActivityJoin;
import org.apache.ibatis.annotations.Mapper;

/**
 * 活动参与记录Mapper接口
 */
@Mapper
public interface ActivityJoinMapper extends BaseMapper<ActivityJoin> {
}
