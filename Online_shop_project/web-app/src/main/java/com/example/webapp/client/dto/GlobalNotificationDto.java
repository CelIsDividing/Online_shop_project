package com.example.webapp.client.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GlobalNotificationDto {
    private Long id;
    private String message;
    private String type;
    private LocalDateTime createdAt;
}
