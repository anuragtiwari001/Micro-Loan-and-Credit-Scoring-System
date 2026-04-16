package com.gla.user.entity;

import com.gla.common_service.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * JPA entity for the `user` table in the gla_users database.
 *
 * IMPORTANT:
 *  - `password` is stored as a BCrypt hash — never plaintext.
 *  - `role` is stored as a string ("USER" / "ADMIN") and sourced from
 *    the shared Role enum in common.
 *  - `email` has a unique constraint enforced at both DB and service layer.
 */
@Data
@Entity
@Table(name = "user",
        uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid address")
    @Column(nullable = false, unique = true)
    private String email;

    /** Stored as BCrypt hash — set via UserService.register() */
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
}