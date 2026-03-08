package com.example.webapp.client;

import com.example.webapp.client.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "user-service", url = "${user-service.url:http://localhost:8765/user-service}", configuration = FeignClientConfig.class)
public interface UserServiceClient {

    @GetMapping("/api/users/{id}")
    UserDto getUserById(@PathVariable Long id);

    @PostMapping("/api/users/login")
    UserDto login(@RequestBody LoginRequest request);

    // Loyalty tiers (admin)
    @GetMapping("/api/loyalty-tiers")
    List<LoyaltyTierDto> getAllTiers();

    @PostMapping("/api/loyalty-tiers")
    LoyaltyTierDto createTier(@RequestBody LoyaltyTierRequestDto request);

    @PutMapping("/api/loyalty-tiers/{id}")
    LoyaltyTierDto updateTier(@PathVariable Long id, @RequestBody LoyaltyTierRequestDto request);

    @DeleteMapping("/api/loyalty-tiers/{id}")
    void deleteTier(@PathVariable Long id);

    // Loyalty points for a user (poziva se nakon plaćanja)
    @PostMapping("/api/users/{userId}/loyalty/add-points")
    LoyaltyAccountDto addPoints(@PathVariable Long userId, @RequestBody AddPointsRequestDto request);

    // Notifications
    @GetMapping("/api/notifications/user/{userId}")
    List<NotificationDto> getNotifications(@PathVariable Long userId);

    @GetMapping("/api/notifications/user/{userId}/unread-count")
    java.util.Map<String, Long> getUnreadCount(@PathVariable Long userId);

    @PutMapping("/api/notifications/{id}/read")
    void markAsRead(@PathVariable Long id);

    @PutMapping("/api/notifications/user/{userId}/read-all")
    void markAllAsRead(@PathVariable Long userId);

    @GetMapping("/api/notifications/global")
    List<GlobalNotificationDto> getGlobalNotifications();

    @PostMapping("/api/notifications/global")
    GlobalNotificationDto createGlobalNotification(@RequestBody GlobalNotificationRequestDto request);
}
