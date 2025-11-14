package com.eric.store.auth.bootstrap;

import com.eric.store.auth.entity.Role;
import com.eric.store.auth.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class RoleSeeder implements CommandLineRunner {
    private final RoleRepository roleRepo;

    @Override public void run(String... args) {
        ensure("USER");
        ensure("ADMIN");
    }
    private void ensure(String name) {
        roleRepo.findByName(name).orElseGet(() -> roleRepo.save(new Role(name)));
    }
}