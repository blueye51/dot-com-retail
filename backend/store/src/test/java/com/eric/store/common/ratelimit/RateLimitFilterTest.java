package com.eric.store.common.ratelimit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RateLimitFilterTest {

    private RateLimitFilter filter;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        filter = new RateLimitFilter();
        filterChain = mock(FilterChain.class);
    }

    @Test
    void generalEndpoint_underLimit_passes() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/products/page");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getHeader("X-Rate-Limit-Remaining")).isNotNull();
    }

    @Test
    void generalEndpoint_exceedsLimit_returns429() throws ServletException, IOException {
        // Exhaust the 50-request general bucket
        for (int i = 0; i < 50; i++) {
            MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/products/page");
            MockHttpServletResponse res = new MockHttpServletResponse();
            filter.doFilterInternal(req, res, filterChain);
            assertThat(res.getStatus()).isEqualTo(200);
        }

        // 51st request should be blocked
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/products/page");
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(429);
        assertThat(response.getContentType()).isEqualTo("application/json");
        assertThat(response.getContentAsString()).contains("Too many requests");
        assertThat(response.getHeader("Retry-After")).isNotNull();
    }

    @Test
    void authEndpoint_exceedsLimit_returns429() throws ServletException, IOException {
        // Exhaust the 10-request auth bucket
        for (int i = 0; i < 10; i++) {
            MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/auth/login");
            MockHttpServletResponse res = new MockHttpServletResponse();
            filter.doFilterInternal(req, res, filterChain);
            assertThat(res.getStatus()).isEqualTo(200);
        }

        // 11th request should be blocked
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(429);
        assertThat(response.getContentAsString()).contains("Too many requests");
    }

    @Test
    void authEndpoint_hasStricterLimit_thanGeneral() throws ServletException, IOException {
        // Send 11 auth requests — should fail
        for (int i = 0; i < 10; i++) {
            MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/auth/login");
            MockHttpServletResponse res = new MockHttpServletResponse();
            filter.doFilterInternal(req, res, filterChain);
        }

        MockHttpServletRequest authReq = new MockHttpServletRequest("POST", "/api/auth/login");
        MockHttpServletResponse authRes = new MockHttpServletResponse();
        filter.doFilterInternal(authReq, authRes, filterChain);
        assertThat(authRes.getStatus()).isEqualTo(429);

        // But a general request from the same IP should still work (separate bucket)
        MockHttpServletRequest generalReq = new MockHttpServletRequest("GET", "/api/products/page");
        MockHttpServletResponse generalRes = new MockHttpServletResponse();
        filter.doFilterInternal(generalReq, generalRes, filterChain);
        assertThat(generalRes.getStatus()).isEqualTo(200);
    }

    @Test
    void differentIPs_haveIndependentBuckets() throws ServletException, IOException {
        // Exhaust bucket for IP 1.1.1.1
        for (int i = 0; i < 50; i++) {
            MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/products/page");
            req.setRemoteAddr("1.1.1.1");
            MockHttpServletResponse res = new MockHttpServletResponse();
            filter.doFilterInternal(req, res, filterChain);
        }

        // IP 1.1.1.1 should be blocked
        MockHttpServletRequest blocked = new MockHttpServletRequest("GET", "/api/products/page");
        blocked.setRemoteAddr("1.1.1.1");
        MockHttpServletResponse blockedRes = new MockHttpServletResponse();
        filter.doFilterInternal(blocked, blockedRes, filterChain);
        assertThat(blockedRes.getStatus()).isEqualTo(429);

        // IP 2.2.2.2 should still work
        MockHttpServletRequest allowed = new MockHttpServletRequest("GET", "/api/products/page");
        allowed.setRemoteAddr("2.2.2.2");
        MockHttpServletResponse allowedRes = new MockHttpServletResponse();
        filter.doFilterInternal(allowed, allowedRes, filterChain);
        assertThat(allowedRes.getStatus()).isEqualTo(200);
    }

    @Test
    void xForwardedFor_usesFirstIp() throws ServletException, IOException {
        // Exhaust bucket for proxied IP 10.0.0.1
        for (int i = 0; i < 50; i++) {
            MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/products/page");
            req.addHeader("X-Forwarded-For", "10.0.0.1, 192.168.1.1");
            MockHttpServletResponse res = new MockHttpServletResponse();
            filter.doFilterInternal(req, res, filterChain);
        }

        // Same X-Forwarded-For IP should be blocked
        MockHttpServletRequest blocked = new MockHttpServletRequest("GET", "/api/products/page");
        blocked.addHeader("X-Forwarded-For", "10.0.0.1, 192.168.1.1");
        MockHttpServletResponse blockedRes = new MockHttpServletResponse();
        filter.doFilterInternal(blocked, blockedRes, filterChain);
        assertThat(blockedRes.getStatus()).isEqualTo(429);

        // Different X-Forwarded-For IP should pass
        MockHttpServletRequest allowed = new MockHttpServletRequest("GET", "/api/products/page");
        allowed.addHeader("X-Forwarded-For", "10.0.0.2, 192.168.1.1");
        MockHttpServletResponse allowedRes = new MockHttpServletResponse();
        filter.doFilterInternal(allowed, allowedRes, filterChain);
        assertThat(allowedRes.getStatus()).isEqualTo(200);
    }

    @Test
    void remainingTokensHeader_decrementsCorrectly() throws ServletException, IOException {
        MockHttpServletRequest req1 = new MockHttpServletRequest("GET", "/api/products/page");
        MockHttpServletResponse res1 = new MockHttpServletResponse();
        filter.doFilterInternal(req1, res1, filterChain);
        int remaining1 = Integer.parseInt(res1.getHeader("X-Rate-Limit-Remaining"));

        MockHttpServletRequest req2 = new MockHttpServletRequest("GET", "/api/products/page");
        MockHttpServletResponse res2 = new MockHttpServletResponse();
        filter.doFilterInternal(req2, res2, filterChain);
        int remaining2 = Integer.parseInt(res2.getHeader("X-Rate-Limit-Remaining"));

        assertThat(remaining2).isEqualTo(remaining1 - 1);
    }
}
