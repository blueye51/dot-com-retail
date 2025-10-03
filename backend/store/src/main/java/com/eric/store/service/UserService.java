package com.eric.store.service;

import com.eric.store.dto.ProductDto;
import com.eric.store.entity.Role;
import com.eric.store.entity.User;
import com.eric.store.repository.RoleRepository;
import com.eric.store.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public User findById(UUID id) {
        return userRepository.findById(id).orElseThrow( () -> new RuntimeException("User not found") );
    }

    public void promoteToAdmin(UUID id) {
        User user = findById(id);
        user.getRoles().add(roleRepository.findByName("ADMIN").orElseThrow(() -> new IllegalArgumentException("Role not found")));
        userRepository.save(user);
    }

    public List<String> getAllRolesFromId(UUID id) {
        User user = findById(id);
        return user.getRoles().stream().map(Role::getName).toList();
    }

}
