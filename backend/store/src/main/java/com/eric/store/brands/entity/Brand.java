package com.eric.store.brands.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@Entity
@Table(name = "brands")
@SQLDelete(sql = "UPDATE brands SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
@Getter @Setter
@NoArgsConstructor
public class Brand {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private boolean deleted = false;

    public Brand(String name) {
        this.name = name;
    }
}
