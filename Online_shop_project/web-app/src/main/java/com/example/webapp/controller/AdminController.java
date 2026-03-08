package com.example.webapp.controller;

import com.example.webapp.client.CatalogServiceClient;
import com.example.webapp.client.OrderServiceClient;
import com.example.webapp.client.PaymentServiceClient;
import com.example.webapp.client.UserServiceClient;
import com.example.webapp.client.dto.*;
import com.example.webapp.client.dto.GlobalNotificationRequestDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.example.webapp.controller.LoginController.*;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserServiceClient userServiceClient;
    private final CatalogServiceClient catalogServiceClient;
    private final OrderServiceClient orderServiceClient;
    private final PaymentServiceClient paymentServiceClient;

    private boolean isAdmin(HttpSession session) {
        String role = (String) session.getAttribute(SESSION_USER_ROLE);
        return "ADMIN".equalsIgnoreCase(role);
    }

    private String guard(HttpSession session) {
        if (session.getAttribute(SESSION_USER_ID) == null || !isAdmin(session)) return "redirect:/login";
        return null;
    }

    @GetMapping({"", "/", "/loyalty", "/catalog", "/analytics"})
    public String adminPage(@RequestParam(defaultValue = "loyalty") String tab,
                            Model model, HttpSession session) {
        String redirect = guard(session);
        if (redirect != null) return redirect;

        model.addAttribute("tab", tab);
        model.addAttribute("userName", session.getAttribute(SESSION_USER_NAME));
        model.addAttribute("tiers", userServiceClient.getAllTiers());

        List<AllergenDto> allergens = catalogServiceClient.getAllAllergens();
        model.addAttribute("allergens", allergens);
        model.addAttribute("products", catalogServiceClient.getAllProducts());
        model.addAttribute("extras", catalogServiceClient.getAllExtras());

        try {
            model.addAttribute("topProducts", orderServiceClient.getTopProducts());
        } catch (Exception e) {
            model.addAttribute("topProducts", List.of());
        }
        try {
            model.addAttribute("topExtras", orderServiceClient.getTopExtras());
        } catch (Exception e) {
            model.addAttribute("topExtras", List.of());
        }
        try {
            model.addAttribute("revenueByDay", paymentServiceClient.getRevenue("day"));
        } catch (Exception e) {
            model.addAttribute("revenueByDay", List.of());
        }
        try {
            model.addAttribute("revenueByWeek", paymentServiceClient.getRevenue("week"));
        } catch (Exception e) {
            model.addAttribute("revenueByWeek", List.of());
        }
        try {
            model.addAttribute("revenueByMonth", paymentServiceClient.getRevenue("month"));
        } catch (Exception e) {
            model.addAttribute("revenueByMonth", List.of());
        }
        try {
            model.addAttribute("vipCustomers", paymentServiceClient.getVipCustomers());
        } catch (Exception e) {
            model.addAttribute("vipCustomers", List.of());
        }

        return "admin";
    }

    @PostMapping("/loyalty")
    public String createTier(@RequestParam String name,
                             @RequestParam BigDecimal discountPercent,
                             @RequestParam Integer minPoints,
                             HttpSession session, RedirectAttributes ra) {
        if (!isAdmin(session)) return "redirect:/login";
        try {
            userServiceClient.createTier(new LoyaltyTierRequestDto(name, discountPercent, minPoints));
            userServiceClient.createGlobalNotification(new GlobalNotificationRequestDto(
                    "Novi loyalty nivo dodat: " + name.toUpperCase() + " (popust " + discountPercent + "%, min. " + minPoints + " poena).",
                    "NEW_LOYALTY_TIER"));
            ra.addFlashAttribute("success", "Tier '" + name + "' je uspešno kreiran.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Greška: " + e.getMessage());
        }
        return "redirect:/admin?tab=loyalty";
    }

    @PostMapping("/loyalty/{id}/edit")
    public String updateTier(@PathVariable Long id,
                             @RequestParam String name,
                             @RequestParam BigDecimal discountPercent,
                             @RequestParam Integer minPoints,
                             HttpSession session, RedirectAttributes ra) {
        if (!isAdmin(session)) return "redirect:/login";
        try {
            userServiceClient.updateTier(id, new LoyaltyTierRequestDto(name, discountPercent, minPoints));
            ra.addFlashAttribute("success", "Tier je uspešno ažuriran.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Greška: " + e.getMessage());
        }
        return "redirect:/admin?tab=loyalty";
    }

    @PostMapping("/loyalty/{id}/delete")
    public String deleteTier(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        if (!isAdmin(session)) return "redirect:/login";
        try {
            userServiceClient.deleteTier(id);
            userServiceClient.createGlobalNotification(new GlobalNotificationRequestDto(
                    "Loyalty nivo je uklonjen iz programa.",
                    "LOYALTY_TIER_DELETED"));
            ra.addFlashAttribute("success", "Tier je obrisan.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Greška: " + e.getMessage());
        }
        return "redirect:/admin?tab=loyalty";
    }

    @PostMapping("/catalog/allergens")
    public String createAllergen(@RequestParam String name,
                                 @RequestParam(required = false) String description,
                                 HttpSession session, RedirectAttributes ra) {
        if (!isAdmin(session)) return "redirect:/login";
        try {
            catalogServiceClient.createAllergen(new AllergenRequestDto(name, description));
            ra.addFlashAttribute("success", "Alergen '" + name + "' je dodat.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Greška: " + e.getMessage());
        }
        return "redirect:/admin?tab=catalog";
    }

    @PostMapping("/catalog/allergens/{id}/edit")
    public String updateAllergen(@PathVariable Long id,
                                 @RequestParam String name,
                                 @RequestParam(required = false) String description,
                                 HttpSession session, RedirectAttributes ra) {
        if (!isAdmin(session)) return "redirect:/login";
        try {
            catalogServiceClient.updateAllergen(id, new AllergenRequestDto(name, description));
            ra.addFlashAttribute("success", "Alergen je ažuriran.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Greška: " + e.getMessage());
        }
        return "redirect:/admin?tab=catalog";
    }

    @PostMapping("/catalog/allergens/{id}/delete")
    public String deleteAllergen(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        if (!isAdmin(session)) return "redirect:/login";
        try {
            catalogServiceClient.deleteAllergen(id);
            ra.addFlashAttribute("success", "Alergen je obrisan.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Greška: " + e.getMessage());
        }
        return "redirect:/admin?tab=catalog";
    }

    @PostMapping("/catalog/products")
    public String createProduct(@RequestParam String name,
                                @RequestParam(required = false) String description,
                                @RequestParam BigDecimal price,
                                @RequestParam(required = false) String size,
                                @RequestParam(required = false) Integer calories,
                                @RequestParam(required = false) List<Long> allergenIds,
                                HttpSession session, RedirectAttributes ra) {
        if (!isAdmin(session)) return "redirect:/login";
        try {
            Set<Long> ids = allergenIds != null ? new HashSet<>(allergenIds) : new HashSet<>();
            catalogServiceClient.createProduct(new ProductRequestDto(name, description, price, size, calories, ids));
            userServiceClient.createGlobalNotification(new GlobalNotificationRequestDto(
                    "Novi proizvod dodat u katalog: " + name + " (" + price + " RSD).",
                    "NEW_PRODUCT"));
            ra.addFlashAttribute("success", "Proizvod '" + name + "' je dodat.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Greška: " + e.getMessage());
        }
        return "redirect:/admin?tab=catalog";
    }

    @PostMapping("/catalog/products/{id}/edit")
    public String updateProduct(@PathVariable Long id,
                                @RequestParam String name,
                                @RequestParam(required = false) String description,
                                @RequestParam BigDecimal price,
                                @RequestParam(required = false) String size,
                                @RequestParam(required = false) Integer calories,
                                @RequestParam(required = false) List<Long> allergenIds,
                                HttpSession session, RedirectAttributes ra) {
        if (!isAdmin(session)) return "redirect:/login";
        try {
            Set<Long> ids = allergenIds != null ? new HashSet<>(allergenIds) : new HashSet<>();
            catalogServiceClient.updateProduct(id, new ProductRequestDto(name, description, price, size, calories, ids));
            userServiceClient.createGlobalNotification(new GlobalNotificationRequestDto(
                    "Proizvod iz kataloga je ažuriran: " + name + " (" + price + " RSD).",
                    "PRODUCT_UPDATED"));
            ra.addFlashAttribute("success", "Proizvod je ažuriran.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Greška: " + e.getMessage());
        }
        return "redirect:/admin?tab=catalog";
    }

    @PostMapping("/catalog/products/{id}/delete")
    public String deleteProduct(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        if (!isAdmin(session)) return "redirect:/login";
        try {
            catalogServiceClient.deleteProduct(id);
            ra.addFlashAttribute("success", "Proizvod je obrisan.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Greška: " + e.getMessage());
        }
        return "redirect:/admin?tab=catalog";
    }

    @PostMapping("/catalog/extras")
    public String createExtra(@RequestParam String name,
                              @RequestParam(required = false) String description,
                              @RequestParam BigDecimal price,
                              @RequestParam(required = false) Integer calories,
                              @RequestParam(required = false) List<Long> allergenIds,
                              HttpSession session, RedirectAttributes ra) {
        if (!isAdmin(session)) return "redirect:/login";
        try {
            Set<Long> ids = allergenIds != null ? new HashSet<>(allergenIds) : new HashSet<>();
            catalogServiceClient.createExtra(new ExtraRequestDto(name, description, price, calories, ids));
            userServiceClient.createGlobalNotification(new GlobalNotificationRequestDto(
                    "Novi dodatak dodat u katalog: " + name + " (" + price + " RSD).",
                    "NEW_EXTRA"));
            ra.addFlashAttribute("success", "Dodatak '" + name + "' je dodat.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Greška: " + e.getMessage());
        }
        return "redirect:/admin?tab=catalog";
    }

    @PostMapping("/catalog/extras/{id}/edit")
    public String updateExtra(@PathVariable Long id,
                              @RequestParam String name,
                              @RequestParam(required = false) String description,
                              @RequestParam BigDecimal price,
                              @RequestParam(required = false) Integer calories,
                              @RequestParam(required = false) List<Long> allergenIds,
                              HttpSession session, RedirectAttributes ra) {
        if (!isAdmin(session)) return "redirect:/login";
        try {
            Set<Long> ids = allergenIds != null ? new HashSet<>(allergenIds) : new HashSet<>();
            catalogServiceClient.updateExtra(id, new ExtraRequestDto(name, description, price, calories, ids));
            userServiceClient.createGlobalNotification(new GlobalNotificationRequestDto(
                    "Dodatak iz kataloga je ažuriran: " + name + " (" + price + " RSD).",
                    "EXTRA_UPDATED"));
            ra.addFlashAttribute("success", "Dodatak je ažuriran.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Greška: " + e.getMessage());
        }
        return "redirect:/admin?tab=catalog";
    }

    @PostMapping("/catalog/extras/{id}/delete")
    public String deleteExtra(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        if (!isAdmin(session)) return "redirect:/login";
        try {
            catalogServiceClient.deleteExtra(id);
            ra.addFlashAttribute("success", "Dodatak je obrisan.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Greška: " + e.getMessage());
        }
        return "redirect:/admin?tab=catalog";
    }
}
