package com.example.userservice.controller;

import com.example.userservice.dto.AddPointsRequest;
import com.example.userservice.dto.LoyaltyTierRequest;
import com.example.userservice.model.LoyaltyAccount;
import com.example.userservice.model.LoyaltyTier;
import com.example.userservice.service.LoyaltyTierService;
import com.example.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LoyaltyController {

    private final LoyaltyTierService loyaltyTierService;
    private final UserService userService;

    // ---- Tier management (admin) ----

    @GetMapping("/loyalty-tiers")
    public List<LoyaltyTier> getAllTiers() {
        return loyaltyTierService.findAll();
    }

    @PostMapping("/loyalty-tiers")
    public ResponseEntity<LoyaltyTier> createTier(@Valid @RequestBody LoyaltyTierRequest request) {
        return ResponseEntity.ok(loyaltyTierService.create(request));
    }

    @PutMapping("/loyalty-tiers/{id}")
    public ResponseEntity<LoyaltyTier> updateTier(@PathVariable Long id,
                                                   @Valid @RequestBody LoyaltyTierRequest request) {
        return loyaltyTierService.update(id, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/loyalty-tiers/{id}")
    public ResponseEntity<Void> deleteTier(@PathVariable Long id) {
        loyaltyTierService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ---- User loyalty: add points after purchase ----

    @PostMapping("/users/{userId}/loyalty/add-points")
    public ResponseEntity<LoyaltyAccount> addPoints(@PathVariable Long userId,
                                                     @Valid @RequestBody AddPointsRequest request) {
        return userService.addPointsForPurchase(userId, request.getPurchaseAmount())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
