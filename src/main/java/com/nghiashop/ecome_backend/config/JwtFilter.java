package com.nghiashop.ecome_backend.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getServletPath().startsWith("/api/auth/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getServletPath();
        System.out.println("🔍 Request path: " + path);

        String header = request.getHeader("Authorization");
        System.out.println("🔍 Authorization header: " + header);

        String token = null;
        String email = null;

        if (header != null && header.startsWith("Bearer ")) {
            token = header.substring(7);
            System.out.println("🔍 Extracted token: " + token);

            try {
                email = jwtUtil.extractEmail(token);
                System.out.println("✅ Extracted email: " + email);
            } catch (Exception e) {
                System.out.println("❌ JWT Token extraction failed: " + e.getMessage());
                e.printStackTrace();
            }
        }

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                System.out.println("✅ User details loaded: " + userDetails.getUsername());

                if (jwtUtil.validateToken(token, userDetails)) {
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());

                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);

                    System.out.println("✅ Authentication set for user: " + email);
                } else {
                    System.out.println("❌ Token validation failed for user: " + email);
                }
            } catch (Exception e) {
                System.out.println("❌ Error in authentication: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("⚠️ No email extracted or authentication already set");
        }

        filterChain.doFilter(request, response);
    }
}
