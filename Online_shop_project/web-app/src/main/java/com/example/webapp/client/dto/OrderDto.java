package com.example.webapp.client.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDto {
    private Long id;
    private Long userId;
    private String status;
    private LocalDateTime createdAt;
    private List<OrderItemDto> items;

    public boolean isDraft() { return "DRAFT".equals(status); }
    public boolean isPaid()  { return "PAID".equals(status); }

    public BigDecimal getTotalPrice() {
        if (items == null) return BigDecimal.ZERO;
        return items.stream()
                .map(OrderItemDto::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Data
    public static class OrderItemDto {
        private Long id;
        private Long productId;
        private String productName;
        private BigDecimal price;
        private List<OrderItemExtraDto> extras;

        public BigDecimal getTotalPrice() {
            BigDecimal extrasTotal = extras == null ? BigDecimal.ZERO :
                    extras.stream().map(OrderItemExtraDto::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
            return price == null ? extrasTotal : price.add(extrasTotal);
        }
    }

    @Data
    public static class OrderItemExtraDto {
        private Long id;
        private Long extraId;
        private String extraName;
        private BigDecimal price;
    }
}
