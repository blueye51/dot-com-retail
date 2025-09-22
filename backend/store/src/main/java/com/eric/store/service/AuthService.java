package com.eric.store.service;

import com.eric.store.dto.UserRegister;
import com.eric.store.entity.User;
import com.eric.store.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    final UserRepository userRepository;

    public User register(UserRegister newUser) {
        User user = new User(newUser.email(), newUser.name(), newUser.password() );
        return userRepository.save(user);
    }
}
