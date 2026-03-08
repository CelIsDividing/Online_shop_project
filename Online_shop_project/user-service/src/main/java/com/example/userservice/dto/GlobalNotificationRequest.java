package com.example.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GlobalNotificationRequest {

    @NotBlank
    private String message;

    @NotBlank
    private String type;
}
