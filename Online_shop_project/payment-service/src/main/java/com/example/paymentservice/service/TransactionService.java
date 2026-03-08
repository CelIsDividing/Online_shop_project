package com.example.paymentservice.service;

import com.example.paymentservice.dto.PayRequest;
import com.example.paymentservice.dto.RevenuePeriodProjection;
import com.example.paymentservice.dto.VipCustomerProjection;
import com.example.paymentservice.model.Card;
import com.example.paymentservice.model.PaymentItem;
import com.example.paymentservice.model.Transaction;
import com.example.paymentservice.repository.CardRepository;
import com.example.paymentservice.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;

    @Transactional
    public Transaction pay(PayRequest request) {
        Card card = cardRepository.findById(request.getCardId())
                .orElseThrow(() -> new IllegalArgumentException("Kartica nije pronađena: " + request.getCardId()));

        Transaction transaction = Transaction.builder()
                .orderId(request.getOrderId())
                .userId(request.getUserId())
                .card(card)
                .amount(BigDecimal.ZERO)
                .build();

        List<PaymentItem> items = request.getPaymentItems().stream().map(req ->
                PaymentItem.builder()
                        .transaction(transaction)
                        .orderItemId(req.getOrderItemId())
                        .description(req.getDescription())
                        .amount(req.getAmount())
                        .build()
        ).toList();

        transaction.setPaymentItems(items);

        // Ako je amount eksplicitno prosleđen (sa loyalty popustom), koristimo ga;
        // inače sumiramo stavke.
        BigDecimal total = request.getAmount() != null && request.getAmount().compareTo(BigDecimal.ZERO) > 0
                ? request.getAmount()
                : items.stream().map(PaymentItem::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        transaction.setAmount(total);

        return transactionRepository.save(transaction);
    }

    @Transactional(readOnly = true)
    public Optional<Transaction> findByOrderId(Long orderId) {
        return transactionRepository.findByOrderId(orderId);
    }

    @Transactional(readOnly = true)
    public List<Transaction> findByUserId(Long userId) {
        return transactionRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<RevenuePeriodProjection> getRevenueByDay() {
        return transactionRepository.getRevenueByDay();
    }

    @Transactional(readOnly = true)
    public List<RevenuePeriodProjection> getRevenueByWeek() {
        return transactionRepository.getRevenueByWeek();
    }

    @Transactional(readOnly = true)
    public List<RevenuePeriodProjection> getRevenueByMonth() {
        return transactionRepository.getRevenueByMonth();
    }

    @Transactional(readOnly = true)
    public List<VipCustomerProjection> getVipCustomers() {
        return transactionRepository.getVipCustomers();
    }
}
