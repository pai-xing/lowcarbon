package com.lowcarbon.controller;

import com.lowcarbon.common.Result;
import com.lowcarbon.dto.UserInfoVO;
import com.lowcarbon.dto.UserLoginDTO;
import com.lowcarbon.dto.UserRegisterDTO;
import com.lowcarbon.dto.UserUpdateDTO;
import com.lowcarbon.service.UserService;
import com.lowcarbon.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "用户与认证模块", description = "用户注册、登录、个人中心相关接口")
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Operation(summary = "用户注册", description = "支持用户注册，密码采用BCrypt加密")
    @PostMapping("/register")
    public Result<Map<String, String>> register(@Valid @RequestBody UserRegisterDTO registerDTO) {
        try {
            String token = userService.register(registerDTO);
            Map<String, String> data = new HashMap<>();
            data.put("token", token);
            return Result.success("注册成功", data);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "用户登录", description = "用户登录，返回JWT Token")
    @PostMapping("/login")
    public Result<Map<String, String>> login(@Valid @RequestBody UserLoginDTO loginDTO) {
        try {
            String token = userService.login(loginDTO);
            Map<String, String> data = new HashMap<>();
            data.put("token", token);
            return Result.success("登录成功", data);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "获取用户信息", description = "获取当前登录用户的详细信息，包括积分、减排量、勋章")
    @GetMapping("/info")
    public Result<UserInfoVO> getUserInfo(HttpServletRequest request) {
        try {
            String token = getTokenFromRequest(request);
            Long userId = jwtUtil.getUserIdFromToken(token);
            UserInfoVO userInfo = userService.getUserInfo(userId);
            return Result.success(userInfo);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "更新用户信息", description = "更新用户的昵称、头像、个性签名")
    @PutMapping("/info")
    public Result<Void> updateUserInfo(@RequestBody UserUpdateDTO updateDTO, HttpServletRequest request) {
        try {
            String token = getTokenFromRequest(request);
            Long userId = jwtUtil.getUserIdFromToken(token);
            userService.updateUserInfo(userId, updateDTO);
            return Result.success("更新成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new RuntimeException("未登录或Token无效");
    }
}

