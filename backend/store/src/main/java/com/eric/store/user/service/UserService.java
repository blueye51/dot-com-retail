package com.eric.store.user.service;

import com.eric.store.common.exceptions.AuthProviderConflictException;
import com.eric.store.user.dto.UserLogin;
import com.eric.store.user.dto.UserRegister;
import com.eric.store.auth.entity.Role;
import com.eric.store.common.exceptions.InvalidEmailOrPassword;
import com.eric.store.common.exceptions.NotFoundException;
import com.eric.store.user.entity.User;
import com.eric.store.auth.repository.RoleRepository;
import com.eric.store.user.mapper.UserMapper;
import com.eric.store.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.eric.store.user.entity.AuthProvider;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    @Value("${app.admin.seed-email:}")
    private String adminSeedEmail;


    public void register(UserRegister newUser) {
        if (userRepository.existsByEmail(newUser.email())) {
            throw new RuntimeException("Email already registered");
        }
        User user = userMapper.toUser(newUser);
        // encode password before persisting for security
        user.setPasswordHash(passwordEncoder.encode(newUser.password()));
        Role userRole  = roleRepository.findByName("USER")
                .orElseThrow(
                        () -> new NotFoundException("Role not found, Server falsely started", "USER"));
        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseThrow(
                        () -> new NotFoundException("Role not found, Server falsely started", "ADMIN"));

        user.getRoles().add(userRole);
        if (!adminSeedEmail.isBlank() && newUser.email().equals(adminSeedEmail))
            user.getRoles().add(adminRole);

        userRepository.save(user);
    }

    public User login(UserLogin login) {
        User user = userRepository.findByEmail(login.email())
                .orElseThrow(() -> new InvalidEmailOrPassword("Invalid email or password"));
        if (!passwordEncoder.matches(login.password(), user.getPasswordHash())) {
            throw new InvalidEmailOrPassword("Invalid email or password");
        }
        return user;
    }

    public User findOrCreateOAuth2User(String email, String name, String sub) {
        Optional<User> existing = userRepository.findByEmail(email);

        if (existing.isPresent()) {
            User user = existing.get();
            if (user.getProvider() != AuthProvider.GOOGLE) {
                throw new AuthProviderConflictException("This email is already registered as a " + user.getProvider() + " account");
            }
            user.setName(name);
            if (user.getProviderId() == null) user.setProviderId(sub);
            return userRepository.save(user);
        }

        User user = new User();
        user.setEmail(email);
        user.setName(name);
        user.setProvider(AuthProvider.GOOGLE);
        user.setProviderId(sub);

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new NotFoundException("Role not found, Server falsely started", "USER"));
        user.getRoles().add(userRole);

        if (!adminSeedEmail.isBlank() && email.equals(adminSeedEmail)) {
            Role adminRole = roleRepository.findByName("ADMIN")
                    .orElseThrow(() -> new NotFoundException("Role not found, Server falsely started", "ADMIN"));
            user.getRoles().add(adminRole);
        }

        return userRepository.save(user);
    }

    public User findById(UUID id) {
        return userRepository.findById(id).orElseThrow( () -> new EntityNotFoundException("User not found") );
    }

    public void promoteToAdmin(UUID id) {
        User user = findById(id);
        user.getRoles().add(roleRepository.findByName("ADMIN").orElseThrow(() -> new EntityNotFoundException("Role not found")));
        userRepository.save(user);
    }

    public List<String> getAllRolesFromId(UUID id) {
        User user = findById(id);
        return user.getRoles().stream().map(Role::getName).toList();
    }

}
