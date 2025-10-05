package com.example.payments.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreatePaymentRequest(
        @Expose
        @SerializedName(value = "orderId")
        @NotNull String orderId,

        @Expose
        @SerializedName(value = "amountCents")
        @NotNull @Min(1) Integer amountCents,

        @Expose
        @SerializedName(value = "currency")
        @NotBlank String currency,

        @Expose
        @SerializedName(value = "email")
        @Email String email
) {}
