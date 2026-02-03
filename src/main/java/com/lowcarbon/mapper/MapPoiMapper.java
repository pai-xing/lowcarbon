package com.lowcarbon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lowcarbon.entity.MapPoi;
import org.apache.ibatis.annotations.Mapper;

/**
 * 地图预设点Mapper
 */
@Mapper
public interface MapPoiMapper extends BaseMapper<MapPoi> {
}
