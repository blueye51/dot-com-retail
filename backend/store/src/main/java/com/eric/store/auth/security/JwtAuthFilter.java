package com.eric.store.auth.security;

import com.eric.store.user.entity.User;
import com.eric.store.user.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserService userService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest req,
            HttpServletResponse res,
            FilterChain chain
    ) throws ServletException, IOException {

        String path = req.getRequestURI();
        if (path.startsWith("/api/auth/")) {
            chain.doFilter(req, res);
            return;
        }

        String header = req.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(req, res);
            return;
        }

        String token = header.substring(7);

        try {
            if (!jwtService.isValid(token)) {
                // Treat as missing: do not set authentication
                SecurityContextHolder.clearContext();
                chain.doFilter(req, res);
                return;
            }

            String uuidString = jwtService.subject(token);
            User user = userService.findById(UUID.fromString(uuidString));

            var authorities = jwtService.roles(token).stream()
                    .filter(Objects::nonNull)
                    .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                    .toList();

            var authToken = new UsernamePasswordAuthenticationToken(user, null, authorities);
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
            SecurityContextHolder.getContext().setAuthentication(authToken);

            chain.doFilter(req, res);

        } catch (Exception e) {
            // Any parsing/expired/signature/etc -> treat as missing
            SecurityContextHolder.clearContext();
            chain.doFilter(req, res);
        }
    }
}
