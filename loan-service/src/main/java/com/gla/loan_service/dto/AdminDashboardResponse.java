package com.gla.loan_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminDashboardResponse {

    private Object loan;
    private Object documents;
    private Object creditDetails;
    private String recommendation; // APPROVE / REVIEW / REJECT
}