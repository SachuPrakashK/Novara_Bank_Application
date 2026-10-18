package com.novara.banking.identity;

import java.io.IOException;
import java.time.Duration;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.context.SecurityContextHolder;

@Component
public class AbsoluteSessionLifetimeFilter extends OncePerRequestFilter {
    private final long maximumAgeMillis;
    public AbsoluteSessionLifetimeFilter(@Value("${novara.security.absolute-session-lifetime}") Duration maximumAge) {
        this.maximumAgeMillis = maximumAge.toMillis();
    }
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null && System.currentTimeMillis() - session.getCreationTime() > maximumAgeMillis) {
            session.invalidate();
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/problem+json");
            response.getWriter().write("{\"code\":\"SESSION_EXPIRED\",\"message\":\"The session has expired.\"}");
            return;
        }
        chain.doFilter(request, response);
    }
}
