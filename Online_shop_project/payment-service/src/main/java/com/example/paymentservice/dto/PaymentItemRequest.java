package com.example.paymentservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentItemRequest {

    @NotNull
    private Long orderItemId;

    @NotBlank
    private String description;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal amount;
}
