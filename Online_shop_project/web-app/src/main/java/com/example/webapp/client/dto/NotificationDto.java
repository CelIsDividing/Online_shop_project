package com.example.webapp.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationDto {
    private Long id;
    private Long userId;
    private String message;
    private String type;
    @JsonProperty("isRead")
    private Boolean isRead;
    private LocalDateTime createdAt;
}
