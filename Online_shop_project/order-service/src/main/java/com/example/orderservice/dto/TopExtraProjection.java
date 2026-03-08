package com.example.orderservice.dto;

import java.math.BigDecimal;

public interface TopExtraProjection {
    Long getExtraId();
    String getExtraName();
    Long getTimesUsed();
    BigDecimal getRevenue();
}
