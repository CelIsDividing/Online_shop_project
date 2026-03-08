package com.example.webapp.controller;

import com.example.webapp.client.CatalogServiceClient;
import com.example.webapp.client.OrderServiceClient;
import com.example.webapp.client.PaymentServiceClient;
import com.example.webapp.client.UserServiceClient;
import com.example.webapp.client.dto.*;
import com.example.webapp.dto.CartItemDto;
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
public class CartController {

    private static final String SESSION_CART = "cart";

    private final OrderServiceClient orderServiceClient;
    private final PaymentServiceClient paymentServiceClient;
    private final CatalogServiceClient catalogServiceClient;
    private final UserServiceClient userServiceClient;

    @SuppressWarnings("unchecked")
    private List<CartItemDto> getCart(HttpSession session) {
        List<CartItemDto> cart = (List<CartItemDto>) session.getAttribute(SESSION_CART);
        if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute(SESSION_CART, cart);
        }
        return cart;
    }

    @GetMapping("/cart")
    public String cart(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute(SESSION_USER_ID);
        if (userId == null) return "redirect:/login";

        List<CartItemDto> cart = getCart(session);
        model.addAttribute("cart", cart);
        model.addAttribute("userName", session.getAttribute(SESSION_USER_NAME));
        model.addAttribute("totalPrice", calculateCartTotal(cart));

        List<ProductDto> products = catalogServiceClient.getAllProducts();
        List<ExtraDto> extras = catalogServiceClient.getAllExtras();
        model.addAttribute("products", products);
        model.addAttribute("extras", extras);
        model.addAttribute("productMap", products.stream()
                .collect(Collectors.toMap(ProductDto::getId, p -> p, (a, b) -> a)));
        model.addAttribute("extraMap", extras.stream()
                .collect(Collectors.toMap(ExtraDto::getId, e -> e, (a, b) -> a)));

        return "cart";
    }

    @PostMapping("/cart/items")
    public String addItem(@RequestParam Long productId,
                          @RequestParam String productName,
                          @RequestParam BigDecimal productPrice,
                          @RequestParam(required = false) List<Long> extraIds,
                          @RequestParam(required = false) List<String> extraNames,
                          @RequestParam(required = false) List<BigDecimal> extraPrices,
                          HttpSession session,
                          RedirectAttributes ra) {
        Long userId = (Long) session.getAttribute(SESSION_USER_ID);
        if (userId == null) return "redirect:/login";

        List<CartItemDto.CartExtraDto> extras = new ArrayList<>();
        if (extraIds != null) {
            for (int i = 0; i < extraIds.size(); i++) {
                extras.add(new CartItemDto.CartExtraDto(
                        extraIds.get(i),
                        (extraNames != null && i < extraNames.size()) ? extraNames.get(i) : "",
                        (extraPrices != null && i < extraPrices.size()) ? extraPrices.get(i) : BigDecimal.ZERO
                ));
            }
        }

        CartItemDto item = new CartItemDto(productId, productName, productPrice, extras);
        getCart(session).add(item);
        return "redirect:/cart";
    }

    @PostMapping("/cart/items/{index}/remove")
    public String removeItem(@PathVariable int index, HttpSession session) {
        Long userId = (Long) session.getAttribute(SESSION_USER_ID);
        if (userId == null) return "redirect:/login";

        List<CartItemDto> cart = getCart(session);
        if (index >= 0 && index < cart.size()) {
            cart.remove(index);
        }
        return "redirect:/cart";
    }

    @GetMapping("/cart/pay")
    public String payForm(Model model, HttpSession session, RedirectAttributes ra) {
        Long userId = (Long) session.getAttribute(SESSION_USER_ID);
        if (userId == null) return "redirect:/login";

        List<CartItemDto> cart = getCart(session);
        if (cart.isEmpty()) {
            ra.addFlashAttribute("error", "Dodajte barem jedan proizvod pre plaćanja.");
            return "redirect:/cart";
        }

        BigDecimal originalTotal = calculateCartTotal(cart);
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

        model.addAttribute("cart", cart);
        model.addAttribute("cards", cards);
        model.addAttribute("userName", session.getAttribute(SESSION_USER_NAME));
        model.addAttribute("originalTotal", originalTotal);
        model.addAttribute("discountPercent", discountPercent);
        model.addAttribute("discountAmount", discountAmount);
        model.addAttribute("finalTotal", finalTotal);
        model.addAttribute("tierName", tierName);
        model.addAttribute("loyaltyPoints", loyaltyPoints);
        model.addAttribute("earnedPoints", earnedPoints);

        return "pay-cart";
    }

    @PostMapping("/cart/pay")
    public String pay(@RequestParam Long cardId, HttpSession session, RedirectAttributes ra) {
        Long userId = (Long) session.getAttribute(SESSION_USER_ID);
        if (userId == null) return "redirect:/login";

        List<CartItemDto> cart = getCart(session);
        if (cart.isEmpty()) {
            ra.addFlashAttribute("error", "Korpa je prazna.");
            return "redirect:/cart";
        }

        try {
            OrderDto draft = orderServiceClient.getOrCreateDraft(userId);
            Long orderId = draft.getId();

            for (CartItemDto item : cart) {
                List<AddItemRequestDto.ExtraItemDto> extras = item.getExtras() == null ? List.of() :
                        item.getExtras().stream()
                                .map(e -> new AddItemRequestDto.ExtraItemDto(e.getExtraId(), e.getExtraName(), e.getPrice()))
                                .toList();
                orderServiceClient.addItem(orderId,
                        new AddItemRequestDto(item.getProductId(), item.getProductName(), item.getPrice(), extras));
            }

            OrderDto order = orderServiceClient.markAsPaid(orderId);

            BigDecimal originalTotal = calculateCartTotal(cart);
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

            List<PayRequestDto.PaymentItemDto> paymentItems = new ArrayList<>();
            for (CartItemDto item : cart) {
                paymentItems.add(new PayRequestDto.PaymentItemDto(0L, item.getProductName(),
                        item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO));
                if (item.getExtras() != null) {
                    for (CartItemDto.CartExtraDto e : item.getExtras()) {
                        paymentItems.add(new PayRequestDto.PaymentItemDto(0L,
                                item.getProductName() + " - " + e.getExtraName(),
                                e.getPrice() != null ? e.getPrice() : BigDecimal.ZERO));
                    }
                }
            }

            PayRequestDto payRequest = new PayRequestDto(order.getId(), userId, cardId, finalTotal, paymentItems);
            paymentServiceClient.pay(payRequest);

            try {
                userServiceClient.addPoints(userId, new AddPointsRequestDto(originalTotal));
            } catch (Exception ignored) {}

            session.removeAttribute(SESSION_CART);
            ra.addFlashAttribute("success", "Porudžbina #" + order.getId() + " je uspešno plaćena!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Greška pri plaćanju: " + e.getMessage());
            return "redirect:/cart/pay";
        }

        return "redirect:/orders";
    }

    private BigDecimal calculateCartTotal(List<CartItemDto> cart) {
        if (cart == null) return BigDecimal.ZERO;
        return cart.stream()
                .map(CartItemDto::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
