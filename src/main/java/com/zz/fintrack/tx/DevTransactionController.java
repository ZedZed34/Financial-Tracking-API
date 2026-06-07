package com.zz.fintrack.tx;

import com.zz.fintrack.common.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Dev-only endpoint for seeding test transaction data.
 * Only active when the "dev" profile is enabled.
 */
@Profile("dev")
@RestController
@RequestMapping("/api/transactions/seed")
public class DevTransactionController {

    private final TransactionService service;
    private final SecurityUtils security;

    public DevTransactionController(TransactionService service, SecurityUtils security) {
        this.service = service;
        this.security = security;
    }

    // Create weekly expenses in bulk for the last N weeks (default 26 ~ 6 months)
    @PostMapping("/weekly")
    public ResponseEntity<List<TransactionDtos.View>> seedWeekly(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam Long accountId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "26") int weeks,
            @RequestParam(defaultValue = "USD") String currency,
            @RequestParam(defaultValue = "25.00") BigDecimal amountPerWeek
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                service.seedWeeklyExpenses(
                        security.currentUserId(principal), accountId, categoryId,
                        weeks, currency, amountPerWeek)
        );
    }
}
