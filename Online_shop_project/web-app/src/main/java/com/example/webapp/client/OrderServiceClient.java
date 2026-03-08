package com.example.webapp.client;

import com.example.webapp.client.dto.AddItemRequestDto;
import com.example.webapp.client.dto.OrderDto;
import com.example.webapp.client.dto.TopExtraDto;
import com.example.webapp.client.dto.TopProductDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "order-service", url = "${order-service.url:http://localhost:8765/order-service}", configuration = FeignClientConfig.class)
public interface OrderServiceClient {

    @GetMapping("/api/orders/user/{userId}")
    List<OrderDto> getOrdersByUser(@PathVariable Long userId);

    @GetMapping("/api/orders/{id}")
    OrderDto getOrderById(@PathVariable Long id);

    @PostMapping("/api/orders/user/{userId}/draft")
    OrderDto getOrCreateDraft(@PathVariable Long userId);

    @PostMapping("/api/orders/{orderId}/items")
    OrderDto addItem(@PathVariable Long orderId, @RequestBody AddItemRequestDto request);

    @DeleteMapping("/api/orders/{orderId}/items/{itemId}")
    OrderDto removeItem(@PathVariable Long orderId, @PathVariable Long itemId);

    @PutMapping("/api/orders/{id}/pay")
    OrderDto markAsPaid(@PathVariable Long id);

    @GetMapping("/api/orders/analytics/top-products")
    List<TopProductDto> getTopProducts();

    @GetMapping("/api/orders/analytics/top-extras")
    List<TopExtraDto> getTopExtras();

}
