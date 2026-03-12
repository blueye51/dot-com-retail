package com.eric.store.brands.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "brands")
@Getter @Setter
@NoArgsConstructor
public class Brand {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    public Brand(String name) {
        this.name = name;
    }
}
