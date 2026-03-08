package com.example.webapp.controller;

import com.example.webapp.client.UserServiceClient;
import com.example.webapp.client.dto.GlobalNotificationDto;
import com.example.webapp.client.dto.NotificationDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

import static com.example.webapp.controller.LoginController.SESSION_USER_ID;
import static com.example.webapp.controller.LoginController.SESSION_USER_NAME;

@Controller
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationsController {

    private final UserServiceClient userServiceClient;

    @GetMapping
    public String notificationsPage(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute(SESSION_USER_ID);
        if (userId == null) return "redirect:/login";

        List<NotificationDto> personal = userServiceClient.getNotifications(userId);
        List<GlobalNotificationDto> global = userServiceClient.getGlobalNotifications();

        long unread = userServiceClient.getUnreadCount(userId)
                .getOrDefault("count", 0L);

        model.addAttribute("userName", session.getAttribute(SESSION_USER_NAME));
        model.addAttribute("personal", personal);
        model.addAttribute("global", global);
        model.addAttribute("unreadCount", unread);
        return "notifications";
    }

    @PostMapping("/{id}/read")
    public String markAsRead(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute(SESSION_USER_ID) == null) return "redirect:/login";
        userServiceClient.markAsRead(id);
        return "redirect:/notifications";
    }

    @PostMapping("/read-all")
    public String markAllAsRead(HttpSession session) {
        Long userId = (Long) session.getAttribute(SESSION_USER_ID);
        if (userId == null) return "redirect:/login";
        userServiceClient.markAllAsRead(userId);
        return "redirect:/notifications";
    }
}
