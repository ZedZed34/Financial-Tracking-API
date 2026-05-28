package com.zz.fintrack.tx.dto;

import com.zz.fintrack.tx.TxType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class TransactionDtos {

    public record Create(
            @NotNull Long accountId,
            Long categoryId,
            @NotNull TxType type,
            @NotNull BigDecimal amount,
            @NotNull LocalDate date,
            String note,
            @NotNull String currency,
            BigDecimal fxRateToBase,
            String baseCurrency
    ) {}

    public record View(
            Long id, Long userId, Long accountId, Long categoryId, String type,
            BigDecimal amount, LocalDate date, String note, String currency,
            BigDecimal baseAmount, String baseCurrency
    ) {}

    public record MonthlyReportRow(
            Long categoryId, String categoryName, String type, BigDecimal totalBase
    ) {}

    public record WeeklyTotal(String week, BigDecimal totalExpense) {}
}
