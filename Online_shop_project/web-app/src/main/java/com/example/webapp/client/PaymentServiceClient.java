package com.example.webapp.client;

import com.example.webapp.client.dto.CardDto;
import com.example.webapp.client.dto.CardRequestDto;
import com.example.webapp.client.dto.PayRequestDto;
import com.example.webapp.client.dto.RevenuePeriodDto;
import com.example.webapp.client.dto.VipCustomerDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "payment-service", url = "${payment-service.url:http://localhost:8765/payment-service}", configuration = FeignClientConfig.class)
public interface PaymentServiceClient {

    @GetMapping("/api/payments/cards/user/{userId}")
    List<CardDto> getCardsByUser(@PathVariable Long userId);

    @PostMapping("/api/payments/cards")
    CardDto addCard(@RequestBody CardRequestDto request);

    @DeleteMapping("/api/payments/cards/{id}")
    void deleteCard(@PathVariable Long id);

    @PostMapping("/api/payments/pay")
    Object pay(@RequestBody PayRequestDto request);

    @GetMapping("/api/payments/analytics/revenue")
    List<RevenuePeriodDto> getRevenue(@RequestParam(defaultValue = "day") String period);

    @GetMapping("/api/payments/analytics/vip-customers")
    List<VipCustomerDto> getVipCustomers();
}
