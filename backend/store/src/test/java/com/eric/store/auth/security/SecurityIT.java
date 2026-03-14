package com.eric.store.auth.security;

import com.eric.store.TestcontainersConfig;
import com.eric.store.auth.service.JwtService;
import com.eric.store.user.entity.AuthProvider;
import com.eric.store.user.entity.Role;
import com.eric.store.user.entity.User;
import com.eric.store.user.entity.UserSettings;
import com.eric.store.user.repository.RoleRepository;
import com.eric.store.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfig.class)
class SecurityIT {

    @Autowired MockMvc mockMvc;
    @Autowired JwtService jwtService;
    @Autowired UserRepository userRepository;
    @Autowired RoleRepository roleRepository;

    private User user;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        roleRepository.deleteAll();

        user = new User();
        user.setName("Test User");
        user.setEmail("test@test.com");
        user.setPasswordHash("encoded");
        user.setProvider(AuthProvider.LOCAL);
        user.setEmailVerified(true);

        UserSettings settings = new UserSettings();
        settings.setUser(user);
        user.setSettings(settings);

        user = userRepository.save(user);
    }

    private String tokenFor(List<String> roles, boolean emailVerified) {
        return jwtService.generateAccessToken(
                user.getId().toString(),
                Map.of("roles", roles, "emailVerified", emailVerified)
        );
    }

    // ---- Public endpoints: no token needed ----

    @Test
    void publicEndpoint_noToken_returns200() throws Exception {
        mockMvc.perform(get("/api/products/page"))
                .andExpect(status().isOk());
    }

    @Test
    void publicBrands_noToken_returns200() throws Exception {
        mockMvc.perform(get("/api/brands"))
                .andExpect(status().isOk());
    }


    @Test
    void protectedEndpoint_noToken_returns401() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }


    @Test
    void protectedEndpoint_validToken_returns200() throws Exception {
        String token = tokenFor(List.of("USER"), true);

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@test.com"));
    }

    @Test
    void protectedEndpoint_invalidToken_returns401() throws Exception {
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer not.a.real.token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminEndpoint_noToken_returns401() throws Exception {
        mockMvc.perform(post("/api/admin/users/" + user.getId() + "/grant-admin"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminEndpoint_userRole_returns403() throws Exception {
        String token = tokenFor(List.of("USER"), true);

        mockMvc.perform(post("/api/admin/users/" + user.getId() + "/grant-admin")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminEndpoint_adminRole_returns204() throws Exception {
        Role adminRole = roleRepository.save(new Role("ADMIN"));
        user.setRoles(Set.of(adminRole));
        userRepository.save(user);

        String token = tokenFor(List.of("ADMIN"), true);

        mockMvc.perform(post("/api/admin/users/" + user.getId() + "/grant-admin")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }
}
