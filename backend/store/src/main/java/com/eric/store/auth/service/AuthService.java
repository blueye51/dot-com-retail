package com.eric.store.auth.service;

import com.eric.store.auth.dto.UserLogin;
import com.eric.store.auth.dto.UserRegister;
import com.eric.store.auth.entity.Role;
import com.eric.store.common.exceptions.InvalidEmailOrPassword;
import com.eric.store.user.entity.User;
import com.eric.store.auth.repository.RoleRepository;
import com.eric.store.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    final UserRepository userRepository;
    final PasswordEncoder passwordEncoder;
    final RoleRepository roleRepository;

    public User register(UserRegister newUser) {
        String hashedPassword = passwordEncoder.encode(newUser.password());
        if (userRepository.existsByEmail(newUser.email())) {
            throw new RuntimeException("Email already registered");
        }
        User user = new User(newUser.email(), newUser.name(), hashedPassword );
        Role userRole  = roleRepository.findByName("USER").orElseThrow();
        Role adminRole = roleRepository.findByName("ADMIN").orElseThrow();

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
}
