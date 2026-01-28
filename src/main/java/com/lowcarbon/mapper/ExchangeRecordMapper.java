package com.lowcarbon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lowcarbon.entity.ExchangeRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 积分兑换记录Mapper接口
 */
@Mapper
public interface ExchangeRecordMapper extends BaseMapper<ExchangeRecord> {
}
