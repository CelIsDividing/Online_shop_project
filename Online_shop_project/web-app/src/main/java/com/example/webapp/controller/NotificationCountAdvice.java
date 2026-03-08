package com.example.webapp.controller;

import com.example.webapp.client.UserServiceClient;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import static com.example.webapp.controller.LoginController.SESSION_USER_ID;

@ControllerAdvice
@RequiredArgsConstructor
public class NotificationCountAdvice {

    private final UserServiceClient userServiceClient;

    @ModelAttribute
    public void addUnreadCount(HttpSession session, Model model) {
        try {
            Long userId = (Long) session.getAttribute(SESSION_USER_ID);
            if (userId != null) {
                var result = userServiceClient.getUnreadCount(userId);
                model.addAttribute("unreadNotifCount", result.getOrDefault("count", 0L));
            } else {
                model.addAttribute("unreadNotifCount", 0L);
            }
        } catch (Exception e) {
            model.addAttribute("unreadNotifCount", 0L);
        }
    }
}
