// ════════════════════════════════════════════════════════════════════════════
// FILE: com/gla/loan_service/dto/LoanRequest.java
// ════════════════════════════════════════════════════════════════════════════
package com.gla.loan_service.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * Request body for POST /api/v1/loan/apply.
 *
 * All fields validated with @Valid before the service layer runs.
 * loanType must match the LoanType enum: PERSONAL|HOME|VEHICLE|EDUCATION|BUSINESS|MEDICAL|EMERGENCY
 */
@Data
public class LoanRequest {

    @NotNull(message = "Loan amount is required")
    @Positive(message = "Loan amount must be greater than 0")
    private Double amount;

    @NotNull(message = "Tenure is required")
    @Min(value = 1,  message = "Minimum tenure is 1 month")
    @Max(value = 360, message = "Maximum tenure is 360 months (30 years)")
    private Integer tenure;

    @NotBlank(message = "Loan type is required")
    private String loanType;   // PERSONAL | HOME | VEHICLE | EDUCATION | BUSINESS | MEDICAL | EMERGENCY

    @Size(max = 1000, message = "Purpose description max 1000 characters")
    private String loanPurposeDescription;

    // ── Bank account details ──────────────────────────────────────────────────

    @NotBlank(message = "Bank name is required")
    private String bankName;

    @NotBlank(message = "Account number is required")
    private String accountNumber;

    @NotBlank(message = "Account type is required")
    private String accountType;   // SAVINGS | CURRENT

    @NotBlank(message = "IFSC code is required")
    @Pattern(regexp = "^[A-Z]{4}0[A-Z0-9]{6}$",
            message = "IFSC code format: SBIN0001234")
    private String ifscCode;

    // ── Financial snapshot at application time ────────────────────────────────

    @NotNull(message = "Monthly income is required")
    @Positive(message = "Monthly income must be positive")
    private Double monthlyIncome;

    @PositiveOrZero(message = "Requested EMI cannot be negative")
    private Double requestedEMI;
}



//package com.gla.loan_service.dto;
//
//import lombok.Data;
//
//@Data
//public class LoanRequest {
//
//    private Double amount;
//    private Integer tenure;
//    private String loanType;
//    private String loanPurposeDescription;
//
//    // ✅ BANK ACCOUNT INFO
//    private String bankName;
//    private String accountNumber;
//    private String accountType;
//    private String ifscCode;
//
//    // ✅ FINANCIAL INFO
//    private Double monthlyIncome;
//    private Double requestedEMI;
//}