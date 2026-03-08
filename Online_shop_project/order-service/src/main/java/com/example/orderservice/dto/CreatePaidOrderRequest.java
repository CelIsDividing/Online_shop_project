package com.example.orderservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CreatePaidOrderRequest {

    @NotNull
    private Long userId;

    @Valid
    private List<AddItemRequest> items = new ArrayList<>();
}
