package com.zz.fintrack.tx;

import com.zz.fintrack.account.Account;
import com.zz.fintrack.category.Category;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity @Table(name="transactions", indexes = {
        @Index(name="idx_tx_user_date", columnList = "user_id,date")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Transaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id", nullable=false)
    private Long userId;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    private Account account;

    @ManyToOne(fetch=FetchType.LAZY)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=10)
    private TxType type;

    @NotNull
    @Column(nullable=false, precision=18, scale=2)
    private BigDecimal amount;

    @NotNull
    @Column(nullable=false)
    private LocalDate date;

    @Size(max=160)
    private String note;

    @NotBlank @Size(min=3,max=3)
    @Column(nullable=false, length=3)
    private String currency;

    // Optional FX snapshot for reporting
    @Column(precision=18, scale=6) private BigDecimal fxRateToBase;
    @Column(length=3) private String baseCurrency;
    @Column(precision=18, scale=2) private BigDecimal baseAmount;

    @Version
    private Long version;
}
