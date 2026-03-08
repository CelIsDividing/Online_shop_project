package com.example.webapp.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayRequestDto {
    private Long orderId;
    private Long userId;
    private Long cardId;
    private BigDecimal amount;
    private List<PaymentItemDto> paymentItems;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentItemDto {
        private Long orderItemId;
        private String description;
        private BigDecimal amount;
    }
}
