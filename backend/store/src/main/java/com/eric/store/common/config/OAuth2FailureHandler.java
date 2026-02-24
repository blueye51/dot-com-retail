package com.eric.store.common.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OAuth2FailureHandler implements AuthenticationFailureHandler {

    @Value("#{'${app.oauth2.allowed-redirect-uris}'.split(',')}")
    private List<String> allowedRedirectUris;

    @Value("${app.oauth2.default-redirect-uri}")
    private String defaultRedirectUri;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException, ServletException {

        String requested = request.getParameter("redirect_uri");
        String redirectUri = (requested != null && allowedRedirectUris.stream().anyMatch(requested::equals))
                ? requested
                : defaultRedirectUri;

        String target = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("error", "oauth2_failed")
                .build()
                .toUriString();

        response.setStatus(HttpServletResponse.SC_FOUND);
        response.sendRedirect(target);
    }
}