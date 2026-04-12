package com.gla.loan_service.controller;

import com.gla.loan_service.dto.LoanRequest;
import com.gla.loan_service.entity.LoanApplication;
import com.gla.loan_service.service.LoanService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/loan")
public class LoanController {

    @Autowired
    private LoanService service;

    @PostMapping("/apply")
    public LoanApplication apply(@RequestBody LoanRequest requestBody,
                                 HttpServletRequest request) {

        String email = (String) request.getAttribute("email");
        String token = request.getHeader("Authorization");
        System.out.println("token : "+token);

        return service.apply(
                email,
                requestBody.getAmount(),
                requestBody.getTenure(),
                requestBody.getLoanType(),
                requestBody.getLoanPurposeDescription(),
                token
        );
    }
}