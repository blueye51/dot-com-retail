package com.eric.store.service;

import com.eric.store.dto.UserLogin;
import com.eric.store.dto.UserRegister;
import com.eric.store.entity.Role;
import com.eric.store.entity.User;
import com.eric.store.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    final UserRepository userRepository;
    final PasswordEncoder passwordEncoder;

    public User register(UserRegister newUser) {
        String hashedPassword = passwordEncoder.encode(newUser.password());
        if (userRepository.existsByEmail(newUser.email())) {
            throw new RuntimeException("Email already registered");
        }
        User user = new User(newUser.email(), newUser.name(), hashedPassword );
        Role role = new Role("USER");
        user.getRoles().add(role);
        return userRepository.save(user);
    }

    public User login(UserLogin login) {
        User user = userRepository.findByEmail(login.email())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));
        if (!passwordEncoder.matches(login.password(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }
        return user;
    }
}
