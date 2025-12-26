package com.lowcarbon.interceptor;

import com.lowcarbon.common.Constants;
import com.lowcarbon.entity.User;
import com.lowcarbon.mapper.UserMapper;
import com.lowcarbon.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserMapper userMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 排除登录和注册接口
        String uri = request.getRequestURI();
        String method = request.getMethod();
        
        // 允许游客访问的接口：注册、登录、文章列表、文章详情（GET请求）
        if (uri.contains("/user/register") || uri.contains("/user/login") 
            || uri.equals("/api/article/list") 
            || (uri.matches("/api/article/\\d+$") && "GET".equals(method))) {
            return true;
        }

        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            response.setStatus(401);
            return false;
        }

        try {
            token = token.substring(7);
            Long userId = jwtUtil.getUserIdFromToken(token);
            User user = userMapper.selectById(userId);
            if (user == null || !Constants.STATUS_NORMAL.equals(user.getStatus())) {
                response.setStatus(401);
                return false;
            }
            // 将用户信息存入request，方便后续使用
            request.setAttribute("userId", userId);
            request.setAttribute("userRole", user.getRole());
            return true;
        } catch (Exception e) {
            response.setStatus(401);
            return false;
        }
    }
}

