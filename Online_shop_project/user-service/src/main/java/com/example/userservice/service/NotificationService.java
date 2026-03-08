package com.example.userservice.service;

import com.example.userservice.model.GlobalNotification;
import com.example.userservice.model.Notification;
import com.example.userservice.repository.GlobalNotificationRepository;
import com.example.userservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final GlobalNotificationRepository globalNotificationRepository;

    public record NotificationEntry(String message, String type) {}

    // ---- Personal ----

    @Transactional
    public Notification createForUser(Long userId, String message, String type) {
        return notificationRepository.save(Notification.builder()
                .userId(userId)
                .message(message)
                .type(type)
                .isRead(false)
                .build());
    }

    @Transactional
    public List<Notification> createBatchForUser(Long userId, List<NotificationEntry> entries) {
        List<Notification> notifications = entries.stream()
                .map(e -> Notification.builder()
                        .userId(userId)
                        .message(e.message())
                        .type(e.type())
                        .isRead(false)
                        .build())
                .toList();
        return notificationRepository.saveAll(notifications);
    }

    @Transactional(readOnly = true)
    public List<Notification> findByUserId(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public long countUnread(Long userId) {
        return notificationRepository.countByUserIdAndIsRead(userId, false);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.markAsRead(notificationId);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId);
    }

    // ---- Global ----

    @Transactional
    public GlobalNotification createGlobal(String message, String type) {
        return globalNotificationRepository.save(GlobalNotification.builder()
                .message(message)
                .type(type)
                .build());
    }

    @Transactional(readOnly = true)
    public List<GlobalNotification> findAllGlobal() {
        return globalNotificationRepository.findAllByOrderByCreatedAtDesc();
    }
}
