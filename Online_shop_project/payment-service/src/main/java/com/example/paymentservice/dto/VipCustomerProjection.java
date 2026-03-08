package com.example.paymentservice.dto;

import java.math.BigDecimal;

public interface VipCustomerProjection {
    Long getUserId();
    String getUserName();
    BigDecimal getTotalSpent();
    Long getOrdersCount();
}
