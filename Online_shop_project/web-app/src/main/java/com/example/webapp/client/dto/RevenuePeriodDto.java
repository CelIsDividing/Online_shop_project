package com.example.webapp.client.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class RevenuePeriodDto {
    private String period;
    private BigDecimal revenue;
    private Long transactionCount;
}
