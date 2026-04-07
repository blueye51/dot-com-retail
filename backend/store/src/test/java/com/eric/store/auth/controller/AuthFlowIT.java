package com.eric.store.auth.controller;

import com.eric.store.TestcontainersConfig;
import com.eric.store.auth.service.TurnstileService;
import com.eric.store.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfig.class)
class AuthFlowIT {

    @Autowired MockMvc mockMvc;
    @Autowired UserRepository userRepository;
    @MockitoBean TurnstileService turnstileService;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        doNothing().when(turnstileService).verifyOrThrow(anyString());
    }

    @Test
    void register_validUser_returns201() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "newuser@test.com",
                                    "password": "Test@1234",
                                    "name": "New User",
                                    "turnstileToken": "fake-token"
                                }
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void register_duplicateEmail_returns409or400() throws Exception {
        // First registration
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "dup@test.com",
                                    "password": "Test@1234",
                                    "name": "User One",
                                    "turnstileToken": "fake-token"
                                }
                                """))
                .andExpect(status().isCreated());

        // Verify email so it counts as taken
        var user = userRepository.findByEmail("dup@test.com").orElseThrow();
        user.setEmailVerified(true);
        userRepository.save(user);

        // Duplicate
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "dup@test.com",
                                    "password": "Test@1234",
                                    "name": "User Two",
                                    "turnstileToken": "fake-token"
                                }
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void register_weakPassword_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "weak@test.com",
                                    "password": "short",
                                    "name": "Weak User",
                                    "turnstileToken": "fake-token"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_missingFields_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "",
                                    "password": "",
                                    "name": "",
                                    "turnstileToken": "fake-token"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_registeredUser_returnsAccessToken() throws Exception {
        // Register first
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "login@test.com",
                                    "password": "Test@1234",
                                    "name": "Login User",
                                    "turnstileToken": "fake-token"
                                }
                                """))
                .andExpect(status().isCreated());

        // Login
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "login@test.com",
                                    "password": "Test@1234",
                                    "turnstileToken": "fake-token"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists());
    }

    @Test
    void login_wrongPassword_returns401() throws Exception {
        // Register
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "wrong@test.com",
                                    "password": "Test@1234",
                                    "name": "Wrong User",
                                    "turnstileToken": "fake-token"
                                }
                                """))
                .andExpect(status().isCreated());

        // Wrong password
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "wrong@test.com",
                                    "password": "WrongPass@1",
                                    "turnstileToken": "fake-token"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_nonExistentUser_returns401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "noone@test.com",
                                    "password": "Test@1234",
                                    "turnstileToken": "fake-token"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }
}
