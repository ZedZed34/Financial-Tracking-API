package com.zz.fintrack.tx;

import com.zz.fintrack.tx.dto.TransactionDtos.Create;
import com.zz.fintrack.tx.dto.TransactionDtos.View;
import com.zz.fintrack.tx.dto.TransactionDtos.MonthlyReportRow;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService service;

    public TransactionController(TransactionService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<View> create(@Valid @RequestBody Create dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @GetMapping
    public Page<View> search(@RequestParam Long userId,
                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
                             @PageableDefault(size = 20, sort = "date", direction = Sort.Direction.DESC)
                             Pageable pageable) {
        return service.search(userId, start, end, pageable);
    }

    @GetMapping("/reports/monthly")
    public List<MonthlyReportRow> monthly(@RequestParam Long userId,
                                          @RequestParam int year,
                                          @RequestParam int month) {
        return service.monthlyReport(userId, year, month);
    }
}
