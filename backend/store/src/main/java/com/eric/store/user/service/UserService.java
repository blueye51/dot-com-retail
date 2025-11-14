package com.eric.store.user.service;

import com.eric.store.auth.entity.Role;
import com.eric.store.user.entity.User;
import com.eric.store.auth.repository.RoleRepository;
import com.eric.store.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

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
