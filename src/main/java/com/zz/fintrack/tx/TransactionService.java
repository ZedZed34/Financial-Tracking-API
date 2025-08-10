package com.zz.fintrack.tx;

import com.zz.fintrack.account.Account;
import com.zz.fintrack.account.AccountRepository;
import com.zz.fintrack.category.Category;
import com.zz.fintrack.category.CategoryRepository;
import com.zz.fintrack.tx.dto.TransactionDtos.Create;
import com.zz.fintrack.tx.dto.TransactionDtos.View;
import com.zz.fintrack.tx.dto.TransactionDtos.MonthlyReportRow;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class TransactionService {
    private final TransactionRepository txRepo;
    private final AccountRepository acctRepo;
    private final CategoryRepository catRepo;

    public TransactionService(TransactionRepository txRepo, AccountRepository acctRepo, CategoryRepository catRepo) {
        this.txRepo = txRepo; this.acctRepo = acctRepo; this.catRepo = catRepo;
    }

    @Transactional
    public View create(Create dto) {
        Account acct = acctRepo.findById(dto.accountId())
                .orElseThrow(() -> new EntityNotFoundException("Account not found"));
        Category cat = dto.categoryId() == null ? null :
                catRepo.findById(dto.categoryId())
                        .orElseThrow(() -> new EntityNotFoundException("Category not found"));

        var tx = Transaction.builder()
                .userId(dto.userId())
                .account(acct)
                .category(cat)
                .type(TxType.valueOf(dto.type()))
                .amount(dto.amount())
                .date(dto.date())
                .note(dto.note())
                .currency(dto.currency())
                .fxRateToBase(dto.fxRateToBase())
                .baseCurrency(dto.baseCurrency())
                .baseAmount(dto.fxRateToBase() != null
                        ? dto.amount().multiply(dto.fxRateToBase()).setScale(2)
                        : null)
                .build();

        var saved = txRepo.save(tx);
        return toView(saved);
    }

    public Page<View> search(Long userId, LocalDate start, LocalDate end, Pageable pageable) {
        return txRepo.findByUserIdAndDateBetween(userId, start, end, pageable).map(this::toView);
    }

    public List<MonthlyReportRow> monthlyReport(Long userId, int year, int month) {
        LocalDate s = LocalDate.of(year, month, 1);
        LocalDate e = s.withDayOfMonth(s.lengthOfMonth());
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

    @Transactional
    public List<View> seedWeeklyExpenses(Long userId, Long accountId, Long categoryId, int weeks,
                                         String currency, BigDecimal amountPerWeek) {
        Account acct = acctRepo.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Account not found"));
        Category cat = categoryId == null ? null :
                catRepo.findById(categoryId).orElseThrow(() -> new EntityNotFoundException("Category not found"));

        LocalDate today = LocalDate.now();
        LocalDate start = today.minusWeeks(weeks - 1);
        var toSave = start.datesUntil(today.plusDays(1))
                .filter(d -> d.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) // one expense per week (Sunday)
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

    public List<com.zz.fintrack.tx.dto.TransactionDtos.WeeklyTotal> weeklyTotals(Long userId, int weeks) {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusWeeks(weeks - 1);
        var data = txRepo.findByUserIdAndDateBetween(userId, start, end, PageRequest.of(0, Integer.MAX_VALUE))
                .stream()
                .collect(Collectors.groupingBy(t -> {
                    WeekFields wf = WeekFields.of(Locale.getDefault());
                    int week = t.getDate().get(wf.weekOfWeekBasedYear());
                    int year = t.getDate().get(wf.weekBasedYear());
                    return year + "-W" + week;
                }, Collectors.mapping(t -> t, Collectors.toList())));

        return data.entrySet().stream()
                .map(e -> new com.zz.fintrack.tx.dto.TransactionDtos.WeeklyTotal(
                        e.getKey(),
                        e.getValue().stream()
                                .filter(t -> t.getType() == TxType.EXPENSE)
                                .map(Transaction::getBaseAmount)
                                .filter(v -> v != null)
                                .reduce(BigDecimal.ZERO, BigDecimal::add)
                ))
                .sorted((a,b) -> a.week().compareTo(b.week()))
                .toList();
    }
}
