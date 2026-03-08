package com.example.webapp.client.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserDto {
    private Long id;
    private String name;
    private String email;
    private String role;
    private LoyaltyInfoDto loyaltyAccount;
    private String token;

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }

    public boolean isKupac() {
        return "KUPAC".equalsIgnoreCase(role);
    }

    @Data
    public static class LoyaltyInfoDto {
        private Long userId;
        private Integer points;
        private LoyaltyTierInfo loyaltyTier;

        public String getTierName() {
            return loyaltyTier != null ? loyaltyTier.getName() : "BRONZE";
        }

        public BigDecimal getDiscountPercent() {
            return loyaltyTier != null ? loyaltyTier.getDiscountPercent() : BigDecimal.ZERO;
        }
    }

    @Data
    public static class LoyaltyTierInfo {
        private Long id;
        private String name;
        private BigDecimal discountPercent;
        private Integer minPoints;
    }
}
