package com.example.userservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LoyaltyTierRequest {

    @NotBlank
    private String name;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal discountPercent;

    @NotNull
    @Min(0)
    private Integer minPoints;
}
