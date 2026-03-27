package com.mmc.collection.security;

import com.mmc.collection.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        if (
                requestURI.startsWith("/api/weather/") ||
                requestURI.equals("/api/user/login") ||
                requestURI.equals("/api/user/register") ||
                requestURI.equals("/api/user/send-code") ||
                requestURI.equals("/api/user/send-reset-code") ||
                requestURI.equals("/api/user/verify-reset-code") ||
                requestURI.equals("/api/user/reset-password")) {
            chain.doFilter(request, response);
            return;
        }

        System.out.println("========== JwtAuthenticationFilter 开始 ==========");
        System.out.println("请求路径: " + request.getRequestURI());
        System.out.println("请求方法: " + request.getMethod());
        System.out.println("请求头 Authorization: " + request.getHeader("Authorization"));


        // 从请求头获取 token
        String token = getTokenFromRequest(request);

        if (StringUtils.hasText(token)) {
            String username = jwtUtil.getUsernameFromToken(token);
            // 如果用户名有效且当前上下文没有认证信息
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 验证 token 是否有效
                if (jwtUtil.validateToken(token, username)) {
                    // 创建一个认证对象（这里没有授予权限，如果需要可以添加）
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(username, null, null);
                    authentication.setDetails(new org.springframework.security.web.authentication.WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }

        chain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}