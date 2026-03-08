package com.example.userservice.dto;

import com.example.userservice.model.LoyaltyAccount;
import com.example.userservice.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponseDto {
    private Long id;
    private String name;
    private String email;
    private String role;
    private LoyaltyAccount loyaltyAccount;
    private String token;

    public static LoginResponseDto from(User user, String token) {
        return new LoginResponseDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getLoyaltyAccount(),
                token
        );
    }
}
