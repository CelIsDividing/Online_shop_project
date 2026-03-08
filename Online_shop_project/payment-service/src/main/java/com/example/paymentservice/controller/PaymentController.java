package com.example.paymentservice.controller;

import com.example.paymentservice.dto.PayRequest;
import com.example.paymentservice.dto.RevenuePeriodProjection;
import com.example.paymentservice.dto.VipCustomerProjection;
import com.example.paymentservice.model.Transaction;
import com.example.paymentservice.service.TransactionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final TransactionService transactionService;

    @PostMapping("/pay")
    public ResponseEntity<Transaction> pay(@Valid @RequestBody PayRequest request,
                                           HttpServletRequest httpRequest) {
        if (!isAuthorizedFor(request.getUserId(), httpRequest))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        Transaction transaction = transactionService.pay(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<Transaction> getByOrder(@PathVariable Long orderId) {
        return transactionService.findByOrderId(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Transaction>> getByUser(@PathVariable Long userId,
                                                       HttpServletRequest request) {
        if (!isAuthorizedFor(userId, request)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(transactionService.findByUserId(userId));
    }

    @GetMapping("/analytics/revenue")
    public ResponseEntity<List<RevenuePeriodProjection>> getRevenue(
            @RequestParam(defaultValue = "day") String period,
            HttpServletRequest request) {
        if (!isAdmin(request)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        List<RevenuePeriodProjection> result = switch (period) {
            case "week"  -> transactionService.getRevenueByWeek();
            case "month" -> transactionService.getRevenueByMonth();
            default      -> transactionService.getRevenueByDay();
        };
        return ResponseEntity.ok(result);
    }

    @GetMapping("/analytics/vip-customers")
    public ResponseEntity<List<VipCustomerProjection>> getVipCustomers(HttpServletRequest request) {
        if (!isAdmin(request)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(transactionService.getVipCustomers());
    }

    @GetMapping("/health")
    public String health() {
        return "payment-service OK";
    }

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
