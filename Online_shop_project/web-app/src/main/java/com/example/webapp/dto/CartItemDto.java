package com.example.webapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDto {
    private Long productId;
    private String productName;
    private BigDecimal price;
    private List<CartExtraDto> extras = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartExtraDto {
        private Long extraId;
        private String extraName;
        private BigDecimal price;
    }

    public BigDecimal getTotalPrice() {
        BigDecimal total = price != null ? price : BigDecimal.ZERO;
        if (extras != null) {
            for (CartExtraDto e : extras) {
                total = total.add(e.getPrice() != null ? e.getPrice() : BigDecimal.ZERO);
            }
        }
        return total;
    }
}
