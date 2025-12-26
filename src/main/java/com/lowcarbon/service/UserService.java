package com.lowcarbon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lowcarbon.dto.UserInfoVO;
import com.lowcarbon.dto.UserLoginDTO;
import com.lowcarbon.dto.UserRegisterDTO;
import com.lowcarbon.dto.UserUpdateDTO;
import com.lowcarbon.entity.User;

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
}

