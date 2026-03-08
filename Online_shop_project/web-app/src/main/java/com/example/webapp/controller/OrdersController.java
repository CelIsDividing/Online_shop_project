package com.example.webapp.controller;

import com.example.webapp.client.CatalogServiceClient;
import com.example.webapp.client.OrderServiceClient;
import com.example.webapp.client.PaymentServiceClient;
import com.example.webapp.client.UserServiceClient;
import com.example.webapp.client.dto.*;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.webapp.controller.LoginController.*;

@Controller
@RequiredArgsConstructor
public class OrdersController {

    private final OrderServiceClient orderServiceClient;
    private final CatalogServiceClient catalogServiceClient;
    private final UserServiceClient userServiceClient;
    private final PaymentServiceClient paymentServiceClient;

    @PostMapping("/orders/draft")
    public String createDraft(HttpSession session, RedirectAttributes ra) {
        Long userId = (Long) session.getAttribute(SESSION_USER_ID);
        if (userId == null) return "redirect:/login";
        try {
            OrderDto draft = orderServiceClient.getOrCreateDraft(userId);
            return "redirect:/orders/" + draft.getId();
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Greška pri kreiranju porudžbine: " + e.getMessage());
            return "redirect:/orders";
        }
    }

    @GetMapping("/orders")
    public String orders(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute(SESSION_USER_ID);
        if (userId == null) return "redirect:/login";

        List<OrderDto> orders = orderServiceClient.getOrdersByUser(userId);
        model.addAttribute("orders", orders);
        model.addAttribute("userName", session.getAttribute(SESSION_USER_NAME));

        try {
            UserDto user = userServiceClient.getUserById(userId);
            if (user.getLoyaltyAccount() != null) {
                model.addAttribute("loyalty", user.getLoyaltyAccount());
            }
        } catch (Exception ignored) {}

        addCatalogMaps(model);
        return "orders";
    }

    @GetMapping("/orders/{id}")
    public String orderDetail(@PathVariable Long id, Model model, HttpSession session,
                              RedirectAttributes ra) {
        Long userId = (Long) session.getAttribute(SESSION_USER_ID);
        if (userId == null) return "redirect:/login";

        OrderDto order;
        try {
            order = orderServiceClient.getOrderById(id);
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Porudžbina nije pronađena.");
            return "redirect:/orders";
        }

        if (!order.getUserId().equals(userId)) {
            ra.addFlashAttribute("error", "Pristup odbijen.");
            return "redirect:/orders";
        }

        model.addAttribute("order", order);
        model.addAttribute("userName", session.getAttribute(SESSION_USER_NAME));

        List<ProductDto> allProducts = catalogServiceClient.getAllProducts();
        List<ExtraDto> allExtras = catalogServiceClient.getAllExtras();

        model.addAttribute("products", allProducts);
        model.addAttribute("extras", allExtras);
        model.addAttribute("productMap", allProducts.stream()
                .collect(Collectors.toMap(ProductDto::getId, p -> p, (a, b) -> a)));
        model.addAttribute("extraMap", allExtras.stream()
                .collect(Collectors.toMap(ExtraDto::getId, e -> e, (a, b) -> a)));

        return "order-detail";
    }

    @PostMapping("/orders/{id}/items")
    public String addItem(@PathVariable Long id,
                          @RequestParam Long productId,
                          @RequestParam String productName,
                          @RequestParam BigDecimal productPrice,
                          @RequestParam(required = false) List<Long> extraIds,
                          @RequestParam(required = false) List<String> extraNames,
                          @RequestParam(required = false) List<BigDecimal> extraPrices,
                          HttpSession session,
                          RedirectAttributes ra) {
        Long userId = (Long) session.getAttribute(SESSION_USER_ID);
        if (userId == null) return "redirect:/login";

        List<AddItemRequestDto.ExtraItemDto> extras = new ArrayList<>();
        if (extraIds != null) {
            for (int i = 0; i < extraIds.size(); i++) {
                extras.add(new AddItemRequestDto.ExtraItemDto(
                        extraIds.get(i),
                        (extraNames != null && i < extraNames.size()) ? extraNames.get(i) : "",
                        (extraPrices != null && i < extraPrices.size()) ? extraPrices.get(i) : BigDecimal.ZERO
                ));
            }
        }

        try {
            orderServiceClient.addItem(id, new AddItemRequestDto(productId, productName, productPrice, extras));
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Greška pri dodavanju stavke: " + e.getMessage());
        }
        return "redirect:/orders/" + id;
    }

    @PostMapping("/orders/{oid}/items/{iid}/remove")
    public String removeItem(@PathVariable Long oid, @PathVariable Long iid,
                             HttpSession session, RedirectAttributes ra) {
        Long userId = (Long) session.getAttribute(SESSION_USER_ID);
        if (userId == null) return "redirect:/login";

        try {
            orderServiceClient.removeItem(oid, iid);
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Greška pri uklanjanju stavke: " + e.getMessage());
        }
        return "redirect:/orders/" + oid;
    }

    @GetMapping("/orders/{id}/pay")
    public String payForm(@PathVariable Long id, Model model, HttpSession session, RedirectAttributes ra) {
        Long userId = (Long) session.getAttribute(SESSION_USER_ID);
        if (userId == null) return "redirect:/login";

        OrderDto order;
        try {
            order = orderServiceClient.getOrderById(id);
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Porudžbina nije pronađena.");
            return "redirect:/orders";
        }

        if (!order.getUserId().equals(userId) || !order.isDraft()) {
            ra.addFlashAttribute("error", "Porudžbina nije dostupna za plaćanje.");
            return "redirect:/orders";
        }

        BigDecimal originalTotal = order.getTotalPrice();
        BigDecimal discountPercent = BigDecimal.ZERO;
        String tierName = "BRONZE";
        int loyaltyPoints = 0;

        try {
            UserDto user = userServiceClient.getUserById(userId);
            if (user.getLoyaltyAccount() != null) {
                discountPercent = user.getLoyaltyAccount().getDiscountPercent();
                tierName = user.getLoyaltyAccount().getTierName();
                loyaltyPoints = user.getLoyaltyAccount().getPoints() != null
                        ? user.getLoyaltyAccount().getPoints() : 0;
            }
        } catch (Exception ignored) {}

        BigDecimal discountAmount = originalTotal
                .multiply(discountPercent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal finalTotal = originalTotal.subtract(discountAmount);
        int earnedPoints = originalTotal.multiply(BigDecimal.valueOf(0.10))
                .setScale(0, RoundingMode.FLOOR).intValue();

        List<CardDto> cards = paymentServiceClient.getCardsByUser(userId);

        model.addAttribute("order", order);
        model.addAttribute("cards", cards);
        model.addAttribute("userName", session.getAttribute(SESSION_USER_NAME));
        model.addAttribute("originalTotal", originalTotal);
        model.addAttribute("discountPercent", discountPercent);
        model.addAttribute("discountAmount", discountAmount);
        model.addAttribute("finalTotal", finalTotal);
        model.addAttribute("tierName", tierName);
        model.addAttribute("loyaltyPoints", loyaltyPoints);
        model.addAttribute("earnedPoints", earnedPoints);

        return "pay-order";
    }

    @PostMapping("/orders/{id}/pay")
    public String pay(@PathVariable Long id, @RequestParam Long cardId,
                      HttpSession session, RedirectAttributes ra) {
        Long userId = (Long) session.getAttribute(SESSION_USER_ID);
        if (userId == null) return "redirect:/login";

        try {
            OrderDto order = orderServiceClient.getOrderById(id);

            if (!order.getUserId().equals(userId) || !order.isDraft()) {
                ra.addFlashAttribute("error", "Porudžbina nije dostupna za plaćanje.");
                return "redirect:/orders";
            }

            BigDecimal originalTotal = order.getTotalPrice();
            BigDecimal discountPercent = BigDecimal.ZERO;
            try {
                UserDto user = userServiceClient.getUserById(userId);
                if (user.getLoyaltyAccount() != null) {
                    discountPercent = user.getLoyaltyAccount().getDiscountPercent();
                }
            } catch (Exception ignored) {}

            BigDecimal discountAmount = originalTotal
                    .multiply(discountPercent)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            BigDecimal finalTotal = originalTotal.subtract(discountAmount);

            orderServiceClient.markAsPaid(id);

            List<PayRequestDto.PaymentItemDto> paymentItems = new ArrayList<>();
            if (order.getItems() != null) {
                for (OrderDto.OrderItemDto item : order.getItems()) {
                    paymentItems.add(new PayRequestDto.PaymentItemDto(item.getId(), item.getProductName(),
                            item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO));
                    if (item.getExtras() != null) {
                        for (OrderDto.OrderItemExtraDto e : item.getExtras()) {
                            paymentItems.add(new PayRequestDto.PaymentItemDto(item.getId(),
                                    item.getProductName() + " - " + e.getExtraName(),
                                    e.getPrice() != null ? e.getPrice() : BigDecimal.ZERO));
                        }
                    }
                }
            }

            PayRequestDto payRequest = new PayRequestDto(id, userId, cardId, finalTotal, paymentItems);
            paymentServiceClient.pay(payRequest);

            try {
                userServiceClient.addPoints(userId, new AddPointsRequestDto(originalTotal));
            } catch (Exception ignored) {}

            ra.addFlashAttribute("success", "Porudžbina #" + id + " je uspešno plaćena!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Greška pri plaćanju: " + e.getMessage());
            return "redirect:/orders/" + id + "/pay";
        }

        return "redirect:/orders";
    }

    private void addCatalogMaps(Model model) {
        try {
            List<ProductDto> allProducts = catalogServiceClient.getAllProducts();
            List<ExtraDto> allExtras = catalogServiceClient.getAllExtras();
            model.addAttribute("productMap", allProducts.stream()
                    .collect(Collectors.toMap(ProductDto::getId, p -> p, (a, b) -> a)));
            model.addAttribute("extraMap", allExtras.stream()
                    .collect(Collectors.toMap(ExtraDto::getId, e -> e, (a, b) -> a)));
        } catch (Exception ignored) {}
    }
}
