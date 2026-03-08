package com.example.paymentservice.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CardRequest {

    @NotNull
    private Long userId;

    @NotBlank
    private String cardHolder;

    @NotBlank
    @Size(min = 4, max = 4)
    @Pattern(regexp = "\\d{4}")
    private String lastFourDigits;

    @NotNull
    @Min(1) @Max(12)
    private Integer expiryMonth;

    @NotNull
    @Min(2024)
    private Integer expiryYear;
}
