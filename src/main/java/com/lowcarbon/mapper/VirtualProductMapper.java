package com.lowcarbon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lowcarbon.entity.VirtualProduct;
import org.apache.ibatis.annotations.Mapper;

/**
 * 虚拟商品Mapper接口
 */
@Mapper
public interface VirtualProductMapper extends BaseMapper<VirtualProduct> {
}
