package com.example.webapp.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddItemRequestDto {
    private Long productId;
    private String productName;
    private BigDecimal price;
    private List<ExtraItemDto> extras;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExtraItemDto {
        private Long extraId;
        private String extraName;
        private BigDecimal price;
    }
}
