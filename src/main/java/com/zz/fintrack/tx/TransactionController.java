package com.zz.fintrack.tx;

import com.zz.fintrack.tx.dto.TransactionDtos.Create;
import com.zz.fintrack.tx.dto.TransactionDtos.MonthlyReportRow;
import com.zz.fintrack.tx.dto.TransactionDtos.View;
import com.zz.fintrack.tx.dto.TransactionDtos.WeeklyTotal;
import com.zz.fintrack.common.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService service;
    private final SecurityUtils security;

    public TransactionController(TransactionService service, SecurityUtils security) {
        this.service = service;
        this.security = security;
    }

    @PostMapping
    public ResponseEntity<View> create(@AuthenticationPrincipal UserDetails principal,
                                       @Valid @RequestBody Create dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(security.currentUserId(principal), dto));
    }

    @GetMapping
    public Page<View> search(@AuthenticationPrincipal UserDetails principal,
                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
                             @PageableDefault(size = 20, sort = "date", direction = Sort.Direction.DESC)
                             Pageable pageable) {
        return service.search(security.currentUserId(principal), start, end, pageable);
    }

    @GetMapping("/reports/monthly")
    public List<MonthlyReportRow> monthly(@AuthenticationPrincipal UserDetails principal,
                                          @RequestParam int year,
                                          @RequestParam int month) {
        return service.monthlyReport(security.currentUserId(principal), year, month);
    }

    // Weekly totals for the last N weeks
    @GetMapping("/reports/weekly-totals")
    public List<WeeklyTotal> weeklyTotals(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam(defaultValue = "26") int weeks
    ) {
        return service.weeklyTotals(security.currentUserId(principal), weeks);
    }
}
