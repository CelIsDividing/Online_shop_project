package com.example.webapp.client.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

@Data
public class ProductDto {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String size;
    private Integer calories;
    private Set<AllergenDto> allergens;

    public boolean hasAllergen(Long allergenId) {
        if (allergens == null) return false;
        return allergens.stream().anyMatch(a -> a.getId().equals(allergenId));
    }
}
