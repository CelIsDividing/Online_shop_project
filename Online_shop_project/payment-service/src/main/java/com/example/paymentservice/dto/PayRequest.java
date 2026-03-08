package com.example.paymentservice.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PayRequest {

    @NotNull
    private Long orderId;

    @NotNull
    private Long userId;

    @NotNull
    private Long cardId;

    private BigDecimal amount;

    @NotEmpty
    private List<PaymentItemRequest> paymentItems;
}
