package com.example.webapp.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExtraRequestDto {
    private String name;
    private String description;
    private BigDecimal price;
    private Integer calories;
    private Set<Long> allergenIds;
}
