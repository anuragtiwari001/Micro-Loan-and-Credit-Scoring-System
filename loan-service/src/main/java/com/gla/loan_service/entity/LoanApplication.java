package com.gla.loan_service.entity;
import com.gla.loan_service.enums.LoanStatus;
import com.gla.loan_service.enums.LoanType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private Double amount;
    private Integer tenure;
    private Integer creditScore;
    @Enumerated(EnumType.STRING)
    private LoanType loanType;
    @Column(length = 1000)
    private String loanPurposeDescription;
    @Enumerated(EnumType.STRING)
    private LoanStatus status;
    private LocalDateTime createdAt;
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        status = LoanStatus.PENDING;
    }
}