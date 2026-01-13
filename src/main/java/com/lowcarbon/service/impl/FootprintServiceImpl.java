package com.lowcarbon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lowcarbon.dto.FootprintCreateDTO;
import com.lowcarbon.dto.FootprintVO;
import com.lowcarbon.entity.Footprint;
import com.lowcarbon.entity.User;
import com.lowcarbon.mapper.FootprintMapper;
import com.lowcarbon.mapper.UserMapper;
import com.lowcarbon.service.FootprintService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FootprintServiceImpl implements FootprintService {
    
    @Autowired
    private FootprintMapper footprintMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    // 碳排放系数配置（kg CO2/单位）
    private static final Map<String, BigDecimal> BEHAVIOR_COEFFICIENTS = new HashMap<>();
    static {
        // 交通出行（kg CO2/km）
        BEHAVIOR_COEFFICIENTS.put("BUS", new BigDecimal("0.089"));           // 公交车
        BEHAVIOR_COEFFICIENTS.put("SUBWAY", new BigDecimal("0.055"));        // 地铁
        BEHAVIOR_COEFFICIENTS.put("BIKE", new BigDecimal("0"));              // 骑行
        BEHAVIOR_COEFFICIENTS.put("WALK", new BigDecimal("0"));              // 步行
        BEHAVIOR_COEFFICIENTS.put("CAR", new BigDecimal("0.271"));           // 私家车
        BEHAVIOR_COEFFICIENTS.put("ELECTRIC_CAR", new BigDecimal("0.15"));   // 电动车
        
        // 用电节约（kg CO2/度）
        BEHAVIOR_COEFFICIENTS.put("SAVE_ELEC", new BigDecimal("0.785"));     // 节约用电
        
        // 用水节约（kg CO2/吨）
        BEHAVIOR_COEFFICIENTS.put("SAVE_WATER", new BigDecimal("0.194"));    // 节约用水
        
        // 饮食（kg CO2/次）
        BEHAVIOR_COEFFICIENTS.put("VEGETARIAN", new BigDecimal("0.5"));      // 素食
        BEHAVIOR_COEFFICIENTS.put("REDUCE_WASTE", new BigDecimal("0.3"));    // 减少食物浪费
        
        // 其他
        BEHAVIOR_COEFFICIENTS.put("RECYCLE", new BigDecimal("0.2"));         // 垃圾分类回收
        BEHAVIOR_COEFFICIENTS.put("REUSE_BAG", new BigDecimal("0.1"));       // 使用环保袋
    }
    
    // 单位配置（与行为类型对应）
    private static final Map<String, String> UNIT_BY_TYPE = new HashMap<>();
    static {
        UNIT_BY_TYPE.put("BUS", "km");
        UNIT_BY_TYPE.put("SUBWAY", "km");
        UNIT_BY_TYPE.put("BIKE", "km");
        UNIT_BY_TYPE.put("WALK", "km");
        UNIT_BY_TYPE.put("CAR", "km");
        UNIT_BY_TYPE.put("ELECTRIC_CAR", "km");
        UNIT_BY_TYPE.put("SAVE_ELEC", "度");
        UNIT_BY_TYPE.put("SAVE_WATER", "吨");
        UNIT_BY_TYPE.put("VEGETARIAN", "次");
        UNIT_BY_TYPE.put("REDUCE_WASTE", "次");
        UNIT_BY_TYPE.put("RECYCLE", "次");
        UNIT_BY_TYPE.put("REUSE_BAG", "次");
    }
    
    // 积分计算规则：每减排1kg CO2 = 10积分
    private static final int POINTS_PER_KG = 10;
    
    private boolean isIntegerValue(BigDecimal v) {
        return v != null && v.scale() <= 0;
    }
    
    @Override
    @Transactional
    public FootprintVO createFootprint(FootprintCreateDTO createDTO, Long userId) {
        Footprint footprint = new Footprint();
        BeanUtils.copyProperties(createDTO, footprint);
        footprint.setUserId(userId);
        
        // 行为类型与单位校验
        String type = createDTO.getBehaviorType();
        if (!BEHAVIOR_COEFFICIENTS.containsKey(type)) {
            throw new RuntimeException("行为类型非法");
        }
        String unit = UNIT_BY_TYPE.get(type);
        BigDecimal value = createDTO.getDataValue();
        if ("次".equals(unit)) {
            if (value == null || value.compareTo(BigDecimal.ONE) < 0 || !isIntegerValue(value)) {
                throw new RuntimeException("数据值不合法：‘次’必须为≥1的整数");
            }
        } else {
            if (value == null || value.compareTo(new BigDecimal("0.01")) < 0) {
                throw new RuntimeException("数据值不合法：必须≥0.01");
            }
        }
        
        // 获取碳排放系数
        BigDecimal coefficient = BEHAVIOR_COEFFICIENTS.get(type);
        footprint.setCoefficient(coefficient);
        
        // 计算碳减排量（保留2位小数）
        BigDecimal reductionAmount = createDTO.getDataValue()
                .multiply(coefficient)
                .setScale(2, RoundingMode.HALF_UP);
        footprint.setReductionAmount(reductionAmount);
        
        // 计算获得的积分
        int pointsEarned = reductionAmount.multiply(new BigDecimal(POINTS_PER_KG)).intValue();
        footprint.setPointsEarned(pointsEarned);
        
        // 保存记录
        footprintMapper.insert(footprint);
        
        // 更新用户的总减排量和积分
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setTotalReduction(user.getTotalReduction().add(reductionAmount));
            user.setPoints(user.getPoints() + pointsEarned);
            userMapper.updateById(user);
        }
        
        // 返回创建的记录（转换为VO）
        FootprintVO vo = new FootprintVO();
        BeanUtils.copyProperties(footprint, vo);
        return vo;
    }
    
    @Override
    public IPage<FootprintVO> getFootprintList(Long userId, Integer pageNum, Integer pageSize, 
                                                LocalDate startDate, LocalDate endDate) {
        Page<Footprint> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Footprint> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Footprint::getUserId, userId);
        
        if (startDate != null) {
            wrapper.ge(Footprint::getRecordDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(Footprint::getRecordDate, endDate);
        }
        
        wrapper.orderByDesc(Footprint::getRecordDate, Footprint::getCreateTime);
        
        IPage<Footprint> footprintPage = footprintMapper.selectPage(page, wrapper);
        
        // 转换为VO
        IPage<FootprintVO> voPage = new Page<>(footprintPage.getCurrent(), footprintPage.getSize(), footprintPage.getTotal());
        List<FootprintVO> voList = footprintPage.getRecords().stream().map(footprint -> {
            FootprintVO vo = new FootprintVO();
            BeanUtils.copyProperties(footprint, vo);
            return vo;
        }).collect(Collectors.toList());
        voPage.setRecords(voList);
        
        return voPage;
    }
    
    @Override
    public Map<String, Object> getStatistics(Long userId, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> stats = footprintMapper.getStatistics(userId, startDate, endDate);
        if (stats == null) {
            stats = new HashMap<>();
            stats.put("totalReduction", BigDecimal.ZERO);
            stats.put("totalPoints", 0);
        }
        
        // 处理null值
        if (stats.get("totalReduction") == null) {
            stats.put("totalReduction", BigDecimal.ZERO);
        }
        if (stats.get("totalPoints") == null) {
            stats.put("totalPoints", 0);
        }
        
        // 获取记录总数
        LambdaQueryWrapper<Footprint> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Footprint::getUserId, userId);
        if (startDate != null) {
            wrapper.ge(Footprint::getRecordDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(Footprint::getRecordDate, endDate);
        }
        Long totalRecords = footprintMapper.selectCount(wrapper);
        stats.put("totalRecords", totalRecords);
        
        return stats;
    }
    
    @Override
    public List<Map<String, Object>> getStatisticsByBehaviorType(Long userId) {
        return footprintMapper.getStatisticsByBehaviorType(userId);
    }
    
    @Override
    public List<Map<String, Object>> getDailyStatistics(Long userId, LocalDate startDate, LocalDate endDate) {
        return footprintMapper.getDailyStatistics(userId, startDate, endDate);
    }
    
    @Override
    @Transactional
    public FootprintVO checkin(String behaviorType, Long userId, String remark) {
        // 仅允许单位为“次”的行为进行一键打卡
        String unit = UNIT_BY_TYPE.get(behaviorType);
        if (unit == null) {
            throw new RuntimeException("行为类型非法");
        }
        if (!"次".equals(unit)) {
            throw new RuntimeException("该行为不支持一键打卡");
        }
        
        LocalDate today = LocalDate.now();
        // 幂等校验：同用户+同行为+同日期仅一次
        int exists = footprintMapper.existsCheckin(userId, behaviorType, today);
        if (exists > 0) {
            throw new RuntimeException("今日已完成该行为打卡");
        }
        
        // 默认名称取自行为配置
        Map<String, Map<String, Object>> behaviorTypes = getBehaviorTypes();
        Map<String, Object> cfg = behaviorTypes.get(behaviorType);
        String behaviorName = cfg != null ? String.valueOf(cfg.get("name")) : behaviorType;
        
        // 组装 DTO
        FootprintCreateDTO dto = new FootprintCreateDTO();
        dto.setBehaviorType(behaviorType);
        dto.setBehaviorName(behaviorName);
        dto.setDataValue(BigDecimal.ONE); // 一次
        dto.setRecordDate(today);
        dto.setRemark(remark);
        
        // 复用创建流程（含积分/减排计算与用户累计更新）
        return createFootprint(dto, userId);
    }
    
    @Override
    public List<Map<String, Object>> getCheckinCalendar(Long userId, LocalDate monthStart, LocalDate monthEnd) {
        return footprintMapper.getCheckinCalendar(userId, monthStart, monthEnd);
    }
    
    @Override
    public Map<String, Object> getCheckinStats(Long userId) {
        Map<String, Object> res = new HashMap<>();
        
        // 1) 计算 streak（从今天开始向前连续有打卡的天数）
        int streak = 0;
        LocalDate cursor = LocalDate.now();
        while (true) {
            int cnt = footprintMapper.existsAnyOnDate(userId, cursor);
            if (cnt > 0) {
                streak++;
                cursor = cursor.minusDays(1);
            } else {
                break;
            }
        }
        res.put("streak", streak);
        
        // 2) 累计打卡数
        Long totalCheckins = footprintMapper.getTotalCheckins(userId);
        res.put("totalCheckins", totalCheckins == null ? 0L : totalCheckins);
        
        // 3) 本月达成率 = 本月有打卡天数 / 当月天数
        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);
        LocalDate monthEnd = now.withDayOfMonth(now.lengthOfMonth());
        Long checkedDays = footprintMapper.getMonthlyCheckedDays(userId, monthStart, monthEnd);
        int daysInMonth = now.lengthOfMonth();
        double monthlyRate = (checkedDays == null ? 0 : checkedDays) * 1.0 / daysInMonth;
        res.put("monthlyRate", monthlyRate);
        
        return res;
    }
    
    @Override
    @Transactional
    public FootprintVO updateFootprint(Long id, FootprintCreateDTO updateDTO, Long userId) {
        Footprint existing = footprintMapper.selectById(id);
        if (existing == null || !existing.getUserId().equals(userId)) {
            throw new RuntimeException("记录不存在或无权修改");
        }
        
        // 行为类型与单位校验
        String type = updateDTO.getBehaviorType();
        if (!BEHAVIOR_COEFFICIENTS.containsKey(type)) {
            throw new RuntimeException("行为类型非法");
        }
        String unit = UNIT_BY_TYPE.get(type);
        BigDecimal value = updateDTO.getDataValue();
        if ("次".equals(unit)) {
            if (value == null || value.compareTo(BigDecimal.ONE) < 0 || !isIntegerValue(value)) {
                throw new RuntimeException("数据值不合法：‘次’必须为≥1的整数");
            }
        } else {
            if (value == null || value.compareTo(new BigDecimal("0.01")) < 0) {
                throw new RuntimeException("数据值不合法：必须≥0.01");
            }
        }
        
        // 计算新系数与减排/积分
        BigDecimal coefficient = BEHAVIOR_COEFFICIENTS.get(type);
        BigDecimal newReduction = value
                .multiply(coefficient)
                .setScale(2, RoundingMode.HALF_UP);
        int newPoints = newReduction.multiply(new BigDecimal(POINTS_PER_KG)).intValue();
        
        // 修正用户累计（按差值）
        User user = userMapper.selectById(userId);
        if (user != null) {
            BigDecimal deltaReduction = newReduction.subtract(existing.getReductionAmount());
            int deltaPoints = newPoints - existing.getPointsEarned();
            
            user.setTotalReduction(user.getTotalReduction().add(deltaReduction));
            user.setPoints(user.getPoints() + deltaPoints);
            
            // 不为负数保护
            if (user.getTotalReduction().compareTo(BigDecimal.ZERO) < 0) {
                user.setTotalReduction(BigDecimal.ZERO);
            }
            if (user.getPoints() < 0) {
                user.setPoints(0);
            }
            userMapper.updateById(user);
        }
        
        // 更新记录字段
        existing.setBehaviorType(updateDTO.getBehaviorType());
        existing.setBehaviorName(updateDTO.getBehaviorName());
        existing.setDataValue(updateDTO.getDataValue());
        existing.setRecordDate(updateDTO.getRecordDate());
        existing.setRemark(updateDTO.getRemark());
        existing.setCoefficient(coefficient);
        existing.setReductionAmount(newReduction);
        existing.setPointsEarned(newPoints);
        footprintMapper.updateById(existing);
        
        FootprintVO vo = new FootprintVO();
        BeanUtils.copyProperties(existing, vo);
        return vo;
    }
    
    @Override
    @Transactional
    public void deleteFootprint(Long id, Long userId) {
        Footprint footprint = footprintMapper.selectById(id);
        if (footprint == null || !footprint.getUserId().equals(userId)) {
            throw new RuntimeException("记录不存在或无权删除");
        }
        
        // 删除记录
        footprintMapper.deleteById(id);
        
        // 更新用户的总减排量和积分
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setTotalReduction(user.getTotalReduction().subtract(footprint.getReductionAmount()));
            user.setPoints(user.getPoints() - footprint.getPointsEarned());
            // 确保不为负数
            if (user.getTotalReduction().compareTo(BigDecimal.ZERO) < 0) {
                user.setTotalReduction(BigDecimal.ZERO);
            }
            if (user.getPoints() < 0) {
                user.setPoints(0);
            }
            userMapper.updateById(user);
        }
    }
    
    /**
     * 获取所有行为类型配置（供前端使用）
     */
    public static Map<String, Map<String, Object>> getBehaviorTypes() {
        Map<String, Map<String, Object>> behaviors = new HashMap<>();
        
        // 交通出行
        addBehavior(behaviors, "BUS", "公交出行", "km", "搭乘公交车出行");
        addBehavior(behaviors, "SUBWAY", "地铁出行", "km", "搭乘地铁出行");
        addBehavior(behaviors, "BIKE", "骑行", "km", "骑自行车出行");
        addBehavior(behaviors, "WALK", "步行", "km", "步行出行");
        addBehavior(behaviors, "ELECTRIC_CAR", "电动车出行", "km", "驾驶电动车出行");
        
        // 节能
        addBehavior(behaviors, "SAVE_ELEC", "节约用电", "度", "节约用电量");
        addBehavior(behaviors, "SAVE_WATER", "节约用水", "吨", "节约用水量");
        
        // 饮食
        addBehavior(behaviors, "VEGETARIAN", "素食", "次", "选择素食餐");
        addBehavior(behaviors, "REDUCE_WASTE", "减少浪费", "次", "减少食物浪费");
        
        // 其他
        addBehavior(behaviors, "RECYCLE", "垃圾分类", "次", "垃圾分类回收");
        addBehavior(behaviors, "REUSE_BAG", "环保袋", "次", "使用环保购物袋");
        
        return behaviors;
    }
    
    private static void addBehavior(Map<String, Map<String, Object>> behaviors, 
                                    String type, String name, String unit, String description) {
        Map<String, Object> behavior = new HashMap<>();
        behavior.put("name", name);
        behavior.put("unit", unit);
        behavior.put("description", description);
        behavior.put("coefficient", BEHAVIOR_COEFFICIENTS.get(type));
        behaviors.put(type, behavior);
    }
}
