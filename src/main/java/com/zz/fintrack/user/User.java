package com.zz.fintrack.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity @Table(name="users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Email
    @Column(nullable=false, unique=true, length=120)
    private String email;

    @NotBlank @Size(max=80)
    @Column(nullable=false, length=80)
    private String name;

    // Store a one-way hash of the password (BCrypt). Never return to clients.
    @JsonIgnore
    @Column(name = "password_hash", length = 100)
    private String passwordHash;
}
