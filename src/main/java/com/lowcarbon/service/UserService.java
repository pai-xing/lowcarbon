package com.lowcarbon.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lowcarbon.dto.UserInfoVO;
import com.lowcarbon.dto.UserLoginDTO;
import com.lowcarbon.dto.UserRegisterDTO;
import com.lowcarbon.dto.UserUpdateDTO;
import com.lowcarbon.entity.User;

import java.util.Map;

public interface UserService extends IService<User> {
    /**
     * 用户注册
     */
    String register(UserRegisterDTO registerDTO);

    /**
     * 用户登录
     */
    String login(UserLoginDTO loginDTO);

    /**
     * 获取用户信息（包含积分、减排量、勋章）
     */
    UserInfoVO getUserInfo(Long userId);

    /**
     * 更新用户信息
     */
    void updateUserInfo(Long userId, UserUpdateDTO updateDTO);

    /**
     * 根据用户名查询用户
     */
    User getByUsername(String username);

    /**
     * 管理员-分页查询用户列表
     */
    IPage<User> getUserList(Integer page, Integer pageSize, String username, Integer status);

    /**
     * 管理员-更新用户状态
     */
    void updateUserStatus(Long userId, Integer status);

    /**
     * 管理员-获取统计数据
     */
    Map<String, Object> getStatistics();
}
