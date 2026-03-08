package com.example.webapp.controller;

import com.example.webapp.client.UserServiceClient;
import com.example.webapp.client.dto.LoginRequest;
import com.example.webapp.client.dto.UserDto;
import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class LoginController {

    private final UserServiceClient userServiceClient;

    public static final String SESSION_USER_ID    = "loggedInUserId";
    public static final String SESSION_USER_NAME  = "loggedInUserName";
    public static final String SESSION_USER_ROLE  = "loggedInUserRole";
    public static final String SESSION_JWT_TOKEN  = "jwtToken";

    @GetMapping("/")
    public String home(Model model, HttpSession session, RedirectAttributes ra) {
        Long userId = (Long) session.getAttribute(SESSION_USER_ID);
        if (userId == null) return "redirect:/login";
        try {
            UserDto user = userServiceClient.getUserById(userId);
            model.addAttribute("user", user);
            return "home";
        } catch (FeignException.NotFound e) {
            session.invalidate();
            ra.addFlashAttribute("error", "Korisnički nalog nije pronađen. Molimo prijavite se ponovo.");
            return "redirect:/login";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Servis trenutno nije dostupan. Pokušajte ponovo.");
            return "redirect:/login";
        }
    }

    @GetMapping("/login")
    public String loginPage(HttpSession session) {
        if (session.getAttribute(SESSION_USER_ID) != null) return "redirect:/success";
        return "login";
    }

    @PostMapping("/login")
    public String login(HttpServletRequest request, HttpSession session, RedirectAttributes redirectAttributes) {
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Unesite email i šifru.");
            return "redirect:/login";
        }
        try {
            UserDto user = userServiceClient.login(new LoginRequest(email.trim(), password));
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "Nepravilni kredencijali.");
                return "redirect:/login";
            }
            session.setAttribute(SESSION_USER_ID,   user.getId());
            session.setAttribute(SESSION_USER_NAME, user.getName());
            String role = user.getRole() != null ? user.getRole() : "KUPAC";
            session.setAttribute(SESSION_USER_ROLE,  role);
            session.setAttribute(SESSION_JWT_TOKEN,  user.getToken());
            if ("ADMIN".equalsIgnoreCase(role)) return "redirect:/admin";
            return "redirect:/orders";
        } catch (FeignException e) {
            if (e.status() == 401 || e.status() == 400) {
                redirectAttributes.addFlashAttribute("error", "Nepravilni kredencijali.");
                return "redirect:/login";
            }
            redirectAttributes.addFlashAttribute("error", "Greška pri prijavi: " + e.getMessage());
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Greška pri prijavi: " + e.getMessage());
            return "redirect:/login";
        }
    }

    @GetMapping("/success")
    public String success(HttpSession session) {
        if (session.getAttribute(SESSION_USER_ID) == null) return "redirect:/login";
        return "success";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
