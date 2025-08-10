package com.zz.fintrack.category;

import com.zz.fintrack.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity @Table(name="categories",
        uniqueConstraints=@UniqueConstraint(columnNames={"user_id","name"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Category {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    private User user;

    @NotBlank @Size(max=60)
    @Column(nullable=false, length=60)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=10)
    private CategoryType type;
}
