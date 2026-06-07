package com.zz.fintrack.tx;

import com.zz.fintrack.account.Account;
import com.zz.fintrack.account.AccountService;
import com.zz.fintrack.category.Category;
import com.zz.fintrack.category.CategoryService;
import com.zz.fintrack.tx.dto.TransactionDtos.Create;
import com.zz.fintrack.tx.dto.TransactionDtos.MonthlyReportRow;
import com.zz.fintrack.tx.dto.TransactionDtos.View;
import com.zz.fintrack.tx.dto.TransactionDtos.WeeklyTotal;
import com.zz.fintrack.kafka.AuditProducer;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class TransactionService {
    private final TransactionRepository txRepo;
    private final AccountService accountService;
    private final CategoryService categoryService;
    private final AuditProducer auditProducer;

    public TransactionService(TransactionRepository txRepo, AccountService accountService, CategoryService categoryService, AuditProducer auditProducer) {
        this.txRepo = txRepo;
        this.accountService = accountService;
        this.categoryService = categoryService;
        this.auditProducer = auditProducer;
    }

    @Transactional
    @CacheEvict(value = "accounts", key = "#userId")
    public View create(Long userId, Create dto) {
        Account acct = resolveAccount(dto.accountId(), userId);
        Category cat = resolveCategory(dto.categoryId(), userId);

        var tx = Transaction.builder()
                .userId(userId)
                .account(acct)
                .category(cat)
                .type(dto.type())
                .amount(dto.amount())
                .date(dto.date())
                .note(dto.note())
                .currency(dto.currency())
                .fxRateToBase(dto.fxRateToBase())
                .baseCurrency(dto.baseCurrency())
                .baseAmount(dto.fxRateToBase() != null
                        ? dto.amount().multiply(dto.fxRateToBase()).setScale(2, RoundingMode.HALF_UP)
                        : null)
                .build();

        var saved = txRepo.save(tx);
        
        // Publish Audit Event to Kafka
        String message = String.format("User %d created a %s transaction of %s %s", 
            userId, dto.type(), dto.amount(), dto.currency());
        auditProducer.publishTransactionEvent(message);
        
        return toView(saved);
    }

    public Page<View> search(Long userId, LocalDate start, LocalDate end, Pageable pageable) {
        return txRepo.findByUserIdAndDateBetween(userId, start, end, pageable).map(this::toView);
    }

    public List<MonthlyReportRow> monthlyReport(Long userId, int year, int month) {
        LocalDate s = YearMonth.of(year, month).atDay(1);
        LocalDate e = YearMonth.of(year, month).atEndOfMonth();
        return txRepo.totalsByCategory(userId, s, e).stream()
                .map(p -> new MonthlyReportRow(
                        p.getCategoryId(),
                        p.getCategoryName(),
                        p.getType().name(),
                        p.getTotalBase()
                )).toList();
    }

    private View toView(Transaction t){
        return new View(
                t.getId(), t.getUserId(), t.getAccount().getId(),
                t.getCategory() != null ? t.getCategory().getId() : null,
                t.getType().name(), t.getAmount(), t.getDate(), t.getNote(),
                t.getCurrency(), t.getBaseAmount(), t.getBaseCurrency()
        );
    }

    private Account resolveAccount(Long accountId, Long userId) {
        return accountService.getOwned(accountId, userId);
    }

    private Category resolveCategory(Long categoryId, Long userId) {
        return categoryId == null ? null
                : categoryService.getOwned(categoryId, userId);
    }

    @Transactional
    public List<View> seedWeeklyExpenses(Long userId, Long accountId, Long categoryId, int weeks,
                                         String currency, BigDecimal amountPerWeek) {
        Account acct = resolveAccount(accountId, userId);
        Category cat = resolveCategory(categoryId, userId);

        LocalDate today = LocalDate.now();
        LocalDate start = today.minusWeeks(weeks - 1);
        var toSave = start.datesUntil(today.plusDays(1))
                .filter(d -> d.getDayOfWeek() == DayOfWeek.SUNDAY) // one expense per week (Sunday)
                .map(d -> Transaction.builder()
                        .userId(userId)
                        .account(acct)
                        .category(cat)
                        .type(TxType.EXPENSE)
                        .amount(amountPerWeek)
                        .date(d)
                        .note("Weekly expense")
                        .currency(currency)
                        .baseAmount(amountPerWeek)
                        .baseCurrency(currency)
                        .build())
                .toList();
        return txRepo.saveAll(toSave).stream().map(this::toView).toList();
    }

    public List<WeeklyTotal> weeklyTotals(Long userId, int weeks) {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusWeeks(weeks - 1);
        return txRepo.findByUserIdAndDateBetweenOrderByDateAsc(userId, start, end)
                .stream()
                .collect(Collectors.groupingBy(
                        t -> toWeekKey(t.getDate()),
                        Collectors.filtering(
                                t -> t.getType() == TxType.EXPENSE && t.getBaseAmount() != null,
                                Collectors.reducing(BigDecimal.ZERO, Transaction::getBaseAmount, BigDecimal::add)
                        )
                ))
                .entrySet().stream()
                .map(e -> new WeeklyTotal(e.getKey(), e.getValue()))
                .sorted((a, b) -> a.week().compareTo(b.week()))
                .toList();
    }

    private static String toWeekKey(LocalDate date) {
        WeekFields wf = WeekFields.of(Locale.getDefault());
        return date.get(wf.weekBasedYear()) + "-W"
                + String.format("%02d", date.get(wf.weekOfWeekBasedYear()));
    }
}
