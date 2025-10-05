package com.example.payments.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import jakarta.validation.constraints.NotBlank;

public record ConfirmPaymentRequest(
        @Expose
        @SerializedName(value = "paymentIntentId")
        @NotBlank String paymentIntentId,
        @Expose
        @SerializedName(value = "status")
        @NotBlank String status,
        @Expose
        @SerializedName(value = "error")
        String error
) {}
