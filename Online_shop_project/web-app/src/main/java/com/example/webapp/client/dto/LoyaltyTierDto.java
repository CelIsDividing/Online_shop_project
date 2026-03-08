package com.example.webapp.client.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LoyaltyTierDto {
    private Long id;
    private String name;
    private BigDecimal discountPercent;
    private Integer minPoints;
}
