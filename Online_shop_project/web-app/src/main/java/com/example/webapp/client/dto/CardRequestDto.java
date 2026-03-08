package com.example.webapp.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardRequestDto {
    private Long userId;
    private String cardHolder;
    private String lastFourDigits;
    private Integer expiryMonth;
    private Integer expiryYear;
}
