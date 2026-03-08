package com.example.orderservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class AddItemRequest {

    @NotNull
    private Long productId;

    @NotBlank
    private String productName;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal price;

    private List<OrderItemExtraRequest> extras = new ArrayList<>();
}
