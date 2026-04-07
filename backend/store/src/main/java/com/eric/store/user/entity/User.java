package com.eric.store.user.entity;


import com.eric.store.common.entity.Address;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Entity
@Table(name = "users")
@SQLDelete(sql = "UPDATE users SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String name;

    @ManyToMany
    @JoinTable(
            name="user_roles",
            joinColumns=@JoinColumn(name="user_id"),
            inverseJoinColumns=@JoinColumn(name="role_id")
    )
    private Set<Role> roles = new HashSet<>();

    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider provider = AuthProvider.LOCAL;

    private String providerId;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, optional = false)
    private UserSettings settings;

    private boolean emailVerified = false;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "name", column = @Column(name = "address_name", length = 512)),
            @AttributeOverride(name = "addressLine1", column = @Column(name = "address_line1", length = 512)),
            @AttributeOverride(name = "addressLine2", column = @Column(name = "address_line2", length = 512)),
            @AttributeOverride(name = "city", column = @Column(name = "address_city", length = 512)),
            @AttributeOverride(name = "state", column = @Column(name = "address_state", length = 512)),
            @AttributeOverride(name = "zip", column = @Column(name = "address_zip", length = 512)),
            @AttributeOverride(name = "country", column = @Column(name = "address_country", length = 512)),
    })
    private Address savedAddress;

    @Column(nullable = false)
    private boolean deleted = false;

    //Timestamps automation
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    @PrePersist
    void prePersist() {
        this.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

}
