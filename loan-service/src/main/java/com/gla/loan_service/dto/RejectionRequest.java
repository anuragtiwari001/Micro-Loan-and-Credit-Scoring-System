package com.gla.loan_service.dto;

import com.gla.loan_service.enums.RejectionReason;
import lombok.Data;
import java.time.LocalDate;

@Data
public class RejectionRequest {

    private RejectionReason rejectionReason;  // structured enum reason
    private String rejectionMessage;           // detailed explanation for user
    private LocalDate reapplyEligibleDate;     // when user can apply again
}