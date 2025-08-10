package com.zz.fintrack.tx;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page<Transaction> findByUserIdAndDateBetween(Long userId, LocalDate start, LocalDate end, Pageable pageable);

    // Monthly totals by category
    @Query("""
     select t.category.id as categoryId,
            coalesce(t.category.name, 'Uncategorized') as categoryName,
            t.type as type,
            sum(t.baseAmount) as totalBase
     from Transaction t
     where t.userId = :userId
       and t.date between :start and :end
       and t.baseAmount is not null
     group by t.category.id, t.category.name, t.type
     """)
    List<CategoryMonthlyTotal> totalsByCategory(@Param("userId") Long userId,
                                                @Param("start") LocalDate start,
                                                @Param("end") LocalDate end);

    interface CategoryMonthlyTotal {
        Long getCategoryId();
        String getCategoryName();
        TxType getType();
        java.math.BigDecimal getTotalBase();
    }
}
