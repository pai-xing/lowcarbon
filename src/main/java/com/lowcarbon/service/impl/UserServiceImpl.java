package com.lowcarbon.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lowcarbon.common.Constants;
import com.lowcarbon.dto.AchievementVO;
import com.lowcarbon.dto.UserInfoVO;
import com.lowcarbon.dto.UserLoginDTO;
import com.lowcarbon.dto.UserRegisterDTO;
import com.lowcarbon.dto.UserUpdateDTO;
import com.lowcarbon.entity.Achievement;
import com.lowcarbon.entity.User;
import com.lowcarbon.mapper.AchievementMapper;
import com.lowcarbon.mapper.UserMapper;
import com.lowcarbon.service.UserService;
import com.lowcarbon.util.JwtUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AchievementMapper achievementMapper;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public String register(UserRegisterDTO registerDTO) {
        // 检查用户名是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, registerDTO.getUsername());
        User existUser = userMapper.selectOne(wrapper);
        if (existUser != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 创建新用户
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setNickname(StrUtil.isNotBlank(registerDTO.getNickname()) 
                ? registerDTO.getNickname() 
                : registerDTO.getUsername());
        user.setPoints(0);
        user.setTotalReduction(java.math.BigDecimal.ZERO);
        user.setRole(Constants.ROLE_USER);
        user.setStatus(Constants.STATUS_NORMAL);

        userMapper.insert(user);

        // 生成JWT Token
        return JwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
    }

    @Override
    public String login(UserLoginDTO loginDTO) {
        // 查询用户
        User user = getByUsername(loginDTO.getUsername());
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 验证密码
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 检查用户状态
        if (!Constants.STATUS_NORMAL.equals(user.getStatus())) {
            throw new RuntimeException("账户已被禁用");
        }

        // 生成JWT Token
        return JwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
    }

    @Override
    public UserInfoVO getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        UserInfoVO vo = new UserInfoVO();
        BeanUtils.copyProperties(user, vo);

        // 查询用户的勋章（type=1表示获得勋章）
        LambdaQueryWrapper<Achievement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Achievement::getUserId, userId)
               .eq(Achievement::getType, 1)
               .orderByDesc(Achievement::getCreateTime);
        List<Achievement> achievements = achievementMapper.selectList(wrapper);

        // 转换为VO
        List<AchievementVO> achievementVOs = achievements.stream().map(achievement -> {
            AchievementVO avo = new AchievementVO();
            BeanUtils.copyProperties(achievement, avo);
            return avo;
        }).collect(Collectors.toList());

        vo.setAchievements(achievementVOs);
        return vo;
    }

    @Override
    @Transactional
    public void updateUserInfo(Long userId, UserUpdateDTO updateDTO) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 更新字段
        if (StrUtil.isNotBlank(updateDTO.getNickname())) {
            user.setNickname(updateDTO.getNickname());
        }
        if (StrUtil.isNotBlank(updateDTO.getAvatar())) {
            user.setAvatar(updateDTO.getAvatar());
        }
        if (StrUtil.isNotBlank(updateDTO.getBio())) {
            user.setBio(updateDTO.getBio());
        }

        userMapper.updateById(user);
    }

    @Override
    public User getByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return userMapper.selectOne(wrapper);
    }

    @Override
    public IPage<User> getUserList(Integer page, Integer pageSize, String username, Integer status) {
        // 创建分页对象
        Page<User> userPage = new Page<>(page, pageSize);
        
        // 构建查询条件
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(username)) {
            wrapper.like(User::getUsername, username)
                   .or()
                   .like(User::getNickname, username);
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        wrapper.orderByDesc(User::getCreateTime);
        
        // 分页查询
        IPage<User> result = userMapper.selectPage(userPage, wrapper);
        
        // 清除密码字段
        result.getRecords().forEach(user -> user.setPassword(null));
        
        return result;
    }

    @Override
    @Transactional
    public void updateUserStatus(Long userId, Integer status) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 不允许禁用管理员
        if (Constants.ROLE_ADMIN.equals(user.getRole())) {
            throw new RuntimeException("不允许禁用管理员账户");
        }
        
        user.setStatus(status);
        userMapper.updateById(user);
    }

    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // 用户总数
        Long totalUsers = userMapper.selectCount(null);
        statistics.put("totalUsers", totalUsers);
        
        // 活跃用户数（状态为0的用户）
        LambdaQueryWrapper<User> activeWrapper = new LambdaQueryWrapper<>();
        activeWrapper.eq(User::getStatus, Constants.STATUS_NORMAL);
        Long activeUsers = userMapper.selectCount(activeWrapper);
        statistics.put("activeUsers", activeUsers);
        
        // 总积分
        List<User> allUsers = userMapper.selectList(null);
        Integer totalPoints = allUsers.stream()
                .mapToInt(user -> user.getPoints() != null ? user.getPoints() : 0)
                .sum();
        statistics.put("totalPoints", totalPoints);
        
        // 总减排量
        java.math.BigDecimal totalReduction = allUsers.stream()
                .map(user -> user.getTotalReduction() != null ? user.getTotalReduction() : java.math.BigDecimal.ZERO)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        statistics.put("totalReduction", totalReduction);
        
        // 今日新增用户（简化实现，实际应该查询今天注册的）
        statistics.put("todayNewUsers", 0);
        
        return statistics;
    }
}
