package com.example.webapp.client.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class TopProductDto {
    private Long productId;
    private String productName;
    private Long timesSold;
    private BigDecimal totalRevenue;
}
