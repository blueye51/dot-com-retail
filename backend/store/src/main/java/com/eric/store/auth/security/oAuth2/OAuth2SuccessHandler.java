package com.eric.store.auth.security.oAuth2;

import com.eric.store.auth.service.TokenService;
import com.eric.store.common.util.Cookie;
import com.eric.store.user.entity.User;
import com.eric.store.user.service.UserService;
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

    private final UserService userService;
    private final TokenService tokenService;
    private final OAuth2LoginCodeStore loginCodeStore;
    private final Cookie cookie;

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
        String sub = principal.getAttribute("sub"); //subject id

        if (email == null || name == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing Google profile fields");
            return;
        }
        if (emailVerified != null && !emailVerified) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Google email not verified");
            return;
        }

        User user = userService.findOrCreateOAuth2User(email, name, sub);

        var pair = tokenService.issueTokens(user.getId());
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.makeRefresh(pair.refresh()).toString());

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