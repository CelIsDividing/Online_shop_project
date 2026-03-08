package com.example.webapp.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoyaltyTierRequestDto {
    private String name;
    private BigDecimal discountPercent;
    private Integer minPoints;
}
