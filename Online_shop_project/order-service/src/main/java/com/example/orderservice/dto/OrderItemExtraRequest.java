package com.example.orderservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemExtraRequest {

    @NotNull
    private Long extraId;

    @NotBlank
    private String extraName;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal price;
}
