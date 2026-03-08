package com.example.orderservice.dto;

import java.math.BigDecimal;

public interface TopProductProjection {
    Long getProductId();
    String getProductName();
    Long getTimesSold();
    BigDecimal getTotalRevenue();
}
