package com.example.webapp.client.dto;

import lombok.Data;

@Data
public class LoyaltyAccountDto {
    private Long userId;
    private Integer points;
    private LoyaltyTierDto loyaltyTier;

    public String getTierName() {
        return loyaltyTier != null ? loyaltyTier.getName() : "BRONZE";
    }

    public java.math.BigDecimal getDiscountPercent() {
        return loyaltyTier != null ? loyaltyTier.getDiscountPercent() : java.math.BigDecimal.ZERO;
    }
}
