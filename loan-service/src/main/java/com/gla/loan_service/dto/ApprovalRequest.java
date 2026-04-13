package com.gla.loan_service.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ApprovalRequest {

    //  LOAN OFFER DETAILS
    private Double approvedAmount;
    private Double interestRate;         // e.g. 10.5
    private Double processingFee;        // e.g. 2000.0

    //  BRANCH VISIT INFO
    private LocalDate branchVisitDate;
    private String branchVisitTimeSlot;  // e.g. "10:00 AM - 11:00 AM"
    private String branchName;
    private String branchAddress;
    private String branchContactNumber;

    //  OPTIONAL REMARKS
    private String approvalRemarks;
}