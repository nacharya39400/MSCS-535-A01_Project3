package com.example.payments.controller;

import com.example.payments.dto.ConfirmPaymentRequest;
import com.example.payments.dto.CreatePaymentRequest;
import com.example.payments.model.Payment;
import com.example.payments.repo.PaymentRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentRepository payments;

    public PaymentController(PaymentRepository payments, @Value("${stripe.secret-key:}") String secretKey) {
        this.payments = payments;
        if (secretKey != null && !secretKey.isBlank()) {
            Stripe.apiKey = secretKey;
        }
    }

    @PostMapping("/create-intent")
    @Transactional
    public ResponseEntity<?> createIntent(@Validated @RequestBody CreatePaymentRequest req) throws StripeException {
        Payment p = new Payment();
        p.setOrderId(req.orderId());
        p.setAmountCents(req.amountCents());
        p.setCurrency(req.currency().toLowerCase());
        p.setBillingEmail(req.email());
        p.setStatus("PENDING");
        payments.saveAndFlush(p);

        Map<String, Object> params = new HashMap<>();
        params.put("amount", req.amountCents());
        params.put("currency", req.currency().toLowerCase());
        if (req.email() != null) {
            params.put("receipt_email", req.email());
        }
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("orderId", req.orderId().toString());
        params.put("metadata", metadata);

        PaymentIntent intent = PaymentIntent.create(params);
        p.setPaymentIntentId(intent.getId());
        payments.save(p);

        Map<String, Object> result = new HashMap<>();
        result.put("clientSecret", intent.getClientSecret());
        result.put("paymentIntentId", intent.getId());
        result.put("paymentId", p.getId().toString());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/record-result")
    @Transactional
    public ResponseEntity<?> recordResult(@Validated @RequestBody ConfirmPaymentRequest req) {
        var pOpt = payments.findByPaymentIntentId(req.paymentIntentId());
        if (pOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var p = pOpt.get();
        String status = req.status().toUpperCase();
        if (status.contains("SUCCEEDED")) {
            p.setStatus("SUCCEEDED");
            p.setLastError(null);
        } else if (status.contains("FAILED") || status.contains("CANCELED")) {
            p.setStatus("FAILED");
            p.setLastError(req.error());
        } else if (status.contains("PROCESSING") || status.contains("REQUIRES")) {
            p.setStatus("PENDING");
        }
        payments.save(p);
        return ResponseEntity.ok(Map.of("ok", true));
    }
}
