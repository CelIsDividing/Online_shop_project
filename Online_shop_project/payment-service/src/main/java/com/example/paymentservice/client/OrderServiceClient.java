package com.example.paymentservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign klijent za poziv order-service-a.
 * Payment-service proverava da li porudžbina postoji pre procesiranja plaćanja.
 */
@FeignClient(name = "order-service", url = "${order-service.url:http://localhost:8001}")
public interface OrderServiceClient {

    @GetMapping("/api/orders/{id}")
    OrderResponse getOrderById(@PathVariable Long id);

    record OrderResponse(Long id, Long userId) {}
}
