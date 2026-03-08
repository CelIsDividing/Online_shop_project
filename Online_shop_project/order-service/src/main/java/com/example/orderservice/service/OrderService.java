package com.example.orderservice.service;

import com.example.orderservice.dto.AddItemRequest;
import com.example.orderservice.model.Order;
import com.example.orderservice.model.OrderItem;
import com.example.orderservice.model.OrderItemExtra;
import com.example.orderservice.repository.OrderItemRepository;
import com.example.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @Transactional(readOnly = true)
    public List<Order> findByUserId(Long userId) {
        return orderRepository.findByUserIdWithItems(userId);
    }

    @Transactional(readOnly = true)
    public Optional<Order> findById(Long id) {
        return orderRepository.findByIdWithItems(id);
    }

    @Transactional
    public Order getOrCreateDraft(Long userId) {
        List<Order> drafts = orderRepository.findDraftsByUserId(userId);
        if (!drafts.isEmpty()) {
            return orderRepository.findByIdWithItems(drafts.get(0).getId()).orElse(drafts.get(0));
        }
        Order order = Order.builder()
                .userId(userId)
                .status("DRAFT")
                .build();
        return orderRepository.save(order);
    }

    @Transactional
    public Order addItem(Long orderId, AddItemRequest request) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Porudžbina nije pronađena: " + orderId));

        if (!"DRAFT".equals(order.getStatus())) {
            throw new IllegalStateException("Nije moguće dodavati stavke u plaćenu porudžbinu.");
        }

        OrderItem item = OrderItem.builder()
                .order(order)
                .productId(request.getProductId())
                .productName(request.getProductName())
                .price(request.getPrice())
                .build();

        if (request.getExtras() != null) {
            List<OrderItemExtra> extras = request.getExtras().stream().map(extraReq ->
                    OrderItemExtra.builder()
                            .orderItem(item)
                            .extraId(extraReq.getExtraId())
                            .extraName(extraReq.getExtraName())
                            .price(extraReq.getPrice())
                            .build()
            ).toList();
            item.setExtras(extras);
        }

        order.getItems().add(item);
        return orderRepository.save(order);
    }

    @Transactional
    public Order removeItem(Long orderId, Long itemId) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Porudžbina nije pronađena: " + orderId));

        if (!"DRAFT".equals(order.getStatus())) {
            throw new IllegalStateException("Nije moguće uklanjati stavke iz plaćene porudžbine.");
        }

        order.getItems().removeIf(item -> item.getId().equals(itemId));
        return orderRepository.save(order);
    }

    @Transactional
    public Optional<Order> markAsPaid(Long orderId) {
        return orderRepository.findByIdWithItems(orderId).map(order -> {
            order.setStatus("PAID");
            return orderRepository.save(order);
        });
    }
}
