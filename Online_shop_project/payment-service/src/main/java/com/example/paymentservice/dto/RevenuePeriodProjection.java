package com.example.paymentservice.dto;

import java.math.BigDecimal;

public interface RevenuePeriodProjection {
    String getPeriod();
    BigDecimal getRevenue();
    Long getTransactionCount();
}
