package com.example.userservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AddPointsRequest {

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal purchaseAmount;
}
