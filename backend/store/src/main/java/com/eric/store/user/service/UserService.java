package com.eric.store.user.service;

import com.eric.store.auth.dto.UserLogin;
import com.eric.store.auth.dto.UserRegister;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public User register(UserRegister newUser) {
        if (userRepository.existsByEmail(newUser.email())) {
            throw new RuntimeException("Email already registered");
        }
        User user = userMapper.toUser(newUser);
        //password hashing and saving done separately for security
        user.setPasswordHash(passwordEncoder.encode(newUser.password()));
        Role userRole  = roleRepository.findByName("USER")
                .orElseThrow(
                        () -> new NotFoundException("Role not found, Server falsely started", "USER"));
        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseThrow(
                        () -> new NotFoundException("Role not found, Server falsely started", "ADMIN"));

        user.getRoles().add(userRole);
        if (newUser.email().equals("eric.rand66@gmail.com")) user.getRoles().add(adminRole); //quick admin for now
        return userRepository.save(user);
    }

    public User login(UserLogin login) {
        User user = userRepository.findByEmail(login.email())
                .orElseThrow(() -> new InvalidEmailOrPassword("Invalid email or password"));
        if (!passwordEncoder.matches(login.password(), user.getPasswordHash())) {
            throw new InvalidEmailOrPassword("Invalid email or password");
        }
        return user;
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
