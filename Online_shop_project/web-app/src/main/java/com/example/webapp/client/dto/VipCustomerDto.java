package com.example.webapp.client.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class VipCustomerDto {
    private Long userId;
    private String userName;
    private BigDecimal totalSpent;
    private Long ordersCount;
}
