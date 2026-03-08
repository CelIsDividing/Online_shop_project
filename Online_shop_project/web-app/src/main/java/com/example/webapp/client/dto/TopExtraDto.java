package com.example.webapp.client.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class TopExtraDto {
    private Long extraId;
    private String extraName;
    private Long timesUsed;
    private BigDecimal revenue;
}
