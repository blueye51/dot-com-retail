package com.eric.store.service;

import com.eric.store.entity.User;
import com.eric.store.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User findById(UUID id) {
        return userRepository.findById(id).orElseThrow( () -> new RuntimeException("User not found") );
    }
}
