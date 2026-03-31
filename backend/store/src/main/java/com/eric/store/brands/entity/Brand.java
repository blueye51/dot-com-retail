package com.eric.store.brands.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SoftDelete;

import java.util.UUID;

@Entity
@Table(name = "brands")
@SoftDelete
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
