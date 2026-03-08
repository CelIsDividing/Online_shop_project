package com.example.userservice.controller;

import com.example.userservice.dto.GlobalNotificationRequest;
import com.example.userservice.model.GlobalNotification;
import com.example.userservice.model.Notification;
import com.example.userservice.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // ---- Personal ----

    @GetMapping("/user/{userId}")
    public List<Notification> getByUser(@PathVariable Long userId) {
        return notificationService.findByUserId(userId);
    }

    @GetMapping("/user/{userId}/unread-count")
    public Map<String, Long> unreadCount(@PathVariable Long userId) {
        return Map.of("count", notificationService.countUnread(userId));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/user/{userId}/read-all")
    public ResponseEntity<Void> markAllAsRead(@PathVariable Long userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.noContent().build();
    }

    // ---- Global ----

    @GetMapping("/global")
    public List<GlobalNotification> getGlobal() {
        return notificationService.findAllGlobal();
    }

    @PostMapping("/global")
    public GlobalNotification createGlobal(@Valid @RequestBody GlobalNotificationRequest request) {
        return notificationService.createGlobal(request.getMessage(), request.getType());
    }
}
