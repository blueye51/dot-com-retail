package com.eric.store.auth.security.oAuth2;

import com.eric.store.auth.entity.Role;
import com.eric.store.auth.repository.RoleRepository;
import com.eric.store.auth.service.TokenService;
import com.eric.store.common.util.Cookie;
import com.eric.store.user.entity.AuthProvider;
import com.eric.store.user.entity.User;
import com.eric.store.user.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TokenService tokenService;
    private final OAuth2LoginCodeStore loginCodeStore;

    @Value("#{'${app.oauth2.allowed-redirect-uris}'.split(',')}")
    private List<String> allowedRedirectUris;

    @Value("${app.oauth2.default-redirect-uri}")
    private String defaultRedirectUri;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        OAuth2User principal = extractPrincipal(authentication);

        String email = principal.getAttribute("email");
        String name  = principal.getAttribute("name");
        Boolean emailVerified = principal.getAttribute("email_verified");
        String sub = principal.getAttribute("sub"); // Google stable subject id (OIDC)

        if (email == null || name == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing Google profile fields");
            return;
        }
        if (emailVerified != null && !emailVerified) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Google email not verified");
            return;
        }

        // Upsert user by email (simple + works with your unique constraint)
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User u = new User();
            u.setEmail(email);
            u.setName(name);
            u.setProvider(AuthProvider.GOOGLE);
            u.setProviderId(sub);

            Role userRole = roleRepository.findByName("USER")
                    .orElseThrow(() -> new IllegalStateException("Role USER missing"));
            u.getRoles().add(userRole);

            return u;
        });

        // If existing user, keep roles; just ensure provider fields are set consistently
        user.setName(name); // keep updated
        if (user.getProvider() == null) user.setProvider(AuthProvider.GOOGLE);
        if (user.getProviderId() == null) user.setProviderId(sub);
        user = userRepository.save(user);

        // Issue tokens (refresh in cookie)
        var pair = tokenService.issueTokens(user.getId());
        response.addHeader(HttpHeaders.SET_COOKIE, Cookie.makeRefresh(pair.refresh(), Duration.ofDays(30)).toString());

        // Create short-lived one-time code for frontend to exchange for access token
        String code = UUID.randomUUID().toString();
        loginCodeStore.save(code, user.getId(), Duration.ofSeconds(60));

        String redirectUri = resolveRedirectUri(request);

        String target = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("code", code)
                .build()
                .toUriString();

        response.setStatus(HttpServletResponse.SC_FOUND);
        response.sendRedirect(target);
    }

    private OAuth2User extractPrincipal(Authentication authentication) {
        Object p = authentication.getPrincipal();
        if (p instanceof OidcUser oidcUser) return oidcUser;
        if (p instanceof OAuth2User oauth2User) return oauth2User;
        throw new IllegalStateException("Unsupported principal: " + p.getClass());
    }

    private String resolveRedirectUri(HttpServletRequest request) {
        String requested = request.getParameter("redirect_uri");
        if (requested == null || requested.isBlank()) return defaultRedirectUri;

        boolean allowed = allowedRedirectUris.stream().anyMatch(requested::equals);
        return allowed ? requested : defaultRedirectUri;
    }
}