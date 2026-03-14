package com.eric.store.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table( name = "roles" )
@Getter
@Setter
@NoArgsConstructor
public class Role {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name;

    public Role(String name) {
        this.name = name;
    }
}
