package com.example.paymentservice.controller;

import com.example.paymentservice.dto.CardRequest;
import com.example.paymentservice.model.Card;
import com.example.paymentservice.service.CardService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Card>> getByUser(@PathVariable Long userId,
                                                HttpServletRequest request) {
        if (!isAuthorizedFor(userId, request)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(cardService.findByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<Card> create(@Valid @RequestBody CardRequest cardRequest,
                                       HttpServletRequest request) {
        if (!isAuthorizedFor(cardRequest.getUserId(), request))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        Card card = cardService.create(cardRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(card);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        return cardService.findById(id)
                .map(card -> {
                    if (!isAuthorizedFor(card.getUserId(), request))
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).<Void>build();
                    cardService.delete(id);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private boolean isAuthorizedFor(Long resourceUserId, HttpServletRequest request) {
        if ("true".equals(request.getHeader("X-Internal"))) return true;
        String xRole = request.getHeader("X-User-Role");
        if ("ADMIN".equals(xRole)) return true;
        String xUserId = request.getHeader("X-User-Id");
        return xUserId != null && xUserId.equals(resourceUserId.toString());
    }
}
