package com.gla.user.entity;

import com.gla.common_service.enums.Role;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String password;
    // getters
    public Long getId() {
        return id;
    }
    @Enumerated(EnumType.STRING)
    private Role role;

}

