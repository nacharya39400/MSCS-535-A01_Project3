package com.example.payments.controller;

import com.example.payments.repo.PaymentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("")
public class HistoryController {

    private final PaymentRepository payments;

    public HistoryController(PaymentRepository payments) {
        this.payments = payments;
    }

    @GetMapping("/get-all-payments")
    public ResponseEntity<Object> getAllPayments() {
        try{
            return ResponseEntity.ok(payments.findAll());
        }
        catch (Exception ex){
            return ResponseEntity.internalServerError().body("unable to process your request.");
        }
    }
}
