package com.eric.store.user.service;

import com.eric.store.common.exceptions.AuthProviderConflictException;
import com.eric.store.user.dto.UserLogin;
import com.eric.store.user.dto.UserProfile;
import com.eric.store.user.dto.UserRegister;
import com.eric.store.user.entity.Role;
import com.eric.store.common.exceptions.InvalidEmailOrPassword;
import com.eric.store.common.exceptions.NotFoundException;
import com.eric.store.user.entity.User;
import com.eric.store.user.repository.RoleRepository;
import com.eric.store.user.mapper.UserMapper;
import com.eric.store.user.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.eric.store.user.entity.AuthProvider;

import com.eric.store.user.entity.UserSettings;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    @Value("${app.admin.seed-email:}")
    private String adminSeedEmail;

    @Transactional
    public void register(UserRegister newUser) {
        if (userRepository.existsByEmailAndEmailVerifiedTrue(newUser.email())) {
            throw new AuthProviderConflictException("Email already registered");
        }
        userRepository.deleteByEmailAndEmailVerifiedFalse(newUser.email());
        User user = userMapper.toUser(newUser);
        user.setPasswordHash(passwordEncoder.encode(newUser.password()));
        createUser(user);
    }

    @Transactional
    public User login(UserLogin login) {
        User user = userRepository.findByEmail(login.email())
                .orElseThrow(() -> new InvalidEmailOrPassword("Invalid email or password"));
        if (AuthProvider.GOOGLE.equals(user.getProvider())) {
            throw new AuthProviderConflictException("This account uses Google sign-in");
        }
        if (!passwordEncoder.matches(login.password(), user.getPasswordHash())) {
            throw new InvalidEmailOrPassword("Invalid email or password");
        }
        return user;
    }

    @Transactional
    public User findOrCreateOAuth2User(String email, String name, String sub) {
        Optional<User> existing = userRepository.findByEmail(email);

        if (existing.isPresent()) {
            User user = existing.get();
            if (user.getProvider() != AuthProvider.GOOGLE) {
                throw new AuthProviderConflictException("This email is already registered as a " + user.getProvider() + " account");
            }
            user.setName(name);
            if (user.getProviderId() == null) user.setProviderId(sub);
            return user;
        }

        User user = new User();
        user.setEmail(email);
        user.setName(name);
        user.setProvider(AuthProvider.GOOGLE);
        user.setProviderId(sub);
        user.setEmailVerified(true);

        return createUser(user);
    }

    @Transactional(readOnly = true)
    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow( () -> new NotFoundException("User not found", id) );
    }

    @Transactional
    public void promoteToAdmin(UUID id) {
        User user = findById(id);
        user.getRoles().add(roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new NotFoundException("Role not found", id)));
    }

    @Transactional(readOnly = true)
    public User findByIdWithRoles(UUID userId) {
        return userRepository.findByIdWithRoles(userId)
                .orElseThrow(() -> new NotFoundException("User not found with roles", userId));
    }


    private Role getRole(String name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new NotFoundException("Role not found, Server falsely started", name));
    }

    @Transactional
    public User createUser(User user) {
        UserSettings settings = new UserSettings();
        settings.setUser(user);

        user.getRoles().add(getRole("USER"));
        if (!adminSeedEmail.isBlank() && user.getEmail().equals(adminSeedEmail)) {
            user.getRoles().add(getRole("ADMIN"));
            settings.setTwoFactorEnabled(true);
            user.setEmailVerified(true);
        }

        user.setSettings(settings);
        return userRepository.save(user);
    }

    @Transactional
    public void setEmailVerified(UUID id) {
        User user = findById(id);
        user.setEmailVerified(true);
    }
}
