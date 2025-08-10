package com.zz.fintrack.account;

import com.zz.fintrack.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "accounts",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id","name"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Account {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private User user;

    @NotBlank @Size(max = 60)
    @Column(nullable = false, length = 60)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AccountType type;

    @NotBlank @Size(min = 3, max = 3)
    @Column(nullable = false, length = 3)
    private String currency;

    @Builder.Default
    @NotNull
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal initialBalance = BigDecimal.ZERO;
}
