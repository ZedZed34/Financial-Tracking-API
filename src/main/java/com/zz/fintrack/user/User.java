package com.zz.fintrack.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

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
}
