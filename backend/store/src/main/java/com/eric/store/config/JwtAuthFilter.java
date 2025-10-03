package com.eric.store.config;

import com.eric.store.entity.User;
import com.eric.store.service.JwtService;
import com.eric.store.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {

        String path = req.getRequestURI();
        if (path.startsWith("/api/auth/")) { chain.doFilter(req, res); return; }

        String auth = req.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ") && SecurityContextHolder.getContext().getAuthentication() == null) {

            String token = auth.substring(7);
            if (jwtService.isValid(token)) {
                String UUIDString = jwtService.subject(token);
                User user = userService.findById(UUID.fromString(UUIDString));

                List<String> roles = jwtService.roles(token);

                Collection<? extends GrantedAuthority> authorities =
                        roles.stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r)).toList();

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(user, null, authorities);
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

        }
        chain.doFilter(req, res);
    }
}
