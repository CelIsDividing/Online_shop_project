package com.example.orderservice.controller;

import com.example.orderservice.dto.AddItemRequest;
import com.example.orderservice.dto.TopExtraProjection;
import com.example.orderservice.dto.TopProductProjection;
import com.example.orderservice.model.Order;
import com.example.orderservice.repository.OrderRepository;
import com.example.orderservice.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Order>> getByUser(@PathVariable Long userId,
                                                 HttpServletRequest request) {
        if (!isAuthorizedFor(userId, request)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(orderService.findByUserId(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getById(@PathVariable Long id, HttpServletRequest request) {
        return orderService.findById(id)
                .map(order -> {
                    if (!isAuthorizedFor(order.getUserId(), request))
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).<Order>build();
                    return ResponseEntity.ok(order);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/user/{userId}/draft")
    public ResponseEntity<Order> getOrCreateDraft(@PathVariable Long userId,
                                                  HttpServletRequest request) {
        if (!isAuthorizedFor(userId, request)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(orderService.getOrCreateDraft(userId));
    }

    @PostMapping("/{orderId}/items")
    public ResponseEntity<Order> addItem(@PathVariable Long orderId,
                                         @Valid @RequestBody AddItemRequest req,
                                         HttpServletRequest request) {
        return orderService.findById(orderId)
                .map(order -> {
                    if (!isAuthorizedFor(order.getUserId(), request))
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).<Order>build();
                    try {
                        return ResponseEntity.ok(orderService.addItem(orderId, req));
                    } catch (IllegalArgumentException | IllegalStateException e) {
                        return ResponseEntity.badRequest().<Order>build();
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{orderId}/items/{itemId}")
    public ResponseEntity<Order> removeItem(@PathVariable Long orderId,
                                             @PathVariable Long itemId,
                                             HttpServletRequest request) {
        return orderService.findById(orderId)
                .map(order -> {
                    if (!isAuthorizedFor(order.getUserId(), request))
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).<Order>build();
                    try {
                        return ResponseEntity.ok(orderService.removeItem(orderId, itemId));
                    } catch (IllegalArgumentException | IllegalStateException e) {
                        return ResponseEntity.badRequest().<Order>build();
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/pay")
    public ResponseEntity<Order> markAsPaid(@PathVariable Long id, HttpServletRequest request) {
        return orderService.findById(id)
                .map(order -> {
                    if (!isAuthorizedFor(order.getUserId(), request))
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).<Order>build();
                    return orderService.markAsPaid(id)
                            .map(ResponseEntity::ok)
                            .orElse(ResponseEntity.notFound().build());
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/analytics/top-products")
    public ResponseEntity<List<TopProductProjection>> getTopProducts(HttpServletRequest request) {
        if (!isAdmin(request)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(orderRepository.findTopProducts());
    }

    @GetMapping("/analytics/top-extras")
    public ResponseEntity<List<TopExtraProjection>> getTopExtras(HttpServletRequest request) {
        if (!isAdmin(request)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(orderRepository.findTopExtras());
    }

    @GetMapping("/health")
    public String health() {
        return "order-service OK";
    }

    /**
     * Returns true when the requesting principal owns the resource OR is an admin.
     * Internal calls (web-app via X-API-Key) set X-Internal=true and are always allowed.
     */
    private boolean isAuthorizedFor(Long resourceUserId, HttpServletRequest request) {
        if ("true".equals(request.getHeader("X-Internal"))) return true;
        String xRole = request.getHeader("X-User-Role");
        if ("ADMIN".equals(xRole)) return true;
        String xUserId = request.getHeader("X-User-Id");
        return xUserId != null && xUserId.equals(resourceUserId.toString());
    }

    private boolean isAdmin(HttpServletRequest request) {
        if ("true".equals(request.getHeader("X-Internal"))) return true;
        return "ADMIN".equals(request.getHeader("X-User-Role"));
    }
}
