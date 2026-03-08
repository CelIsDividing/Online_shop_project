package com.example.webapp.client.dto;

import lombok.Data;

@Data
public class CardDto {
    private Long id;
    private Long userId;
    private String cardHolder;
    private String lastFourDigits;
    private Integer expiryMonth;
    private Integer expiryYear;

    public String getDisplayName() {
        return cardHolder + " **** " + lastFourDigits + " (" + expiryMonth + "/" + expiryYear + ")";
    }
}
