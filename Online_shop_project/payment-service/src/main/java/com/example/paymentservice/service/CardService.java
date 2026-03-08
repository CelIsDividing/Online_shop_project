package com.example.paymentservice.service;

import com.example.paymentservice.dto.CardRequest;
import com.example.paymentservice.model.Card;
import com.example.paymentservice.repository.CardRepository;
import com.example.paymentservice.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final TransactionRepository transactionRepository;

    @Transactional(readOnly = true)
    public List<Card> findByUserId(Long userId) {
        return cardRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Optional<Card> findById(Long id) {
        return cardRepository.findById(id);
    }

    @Transactional
    public Card create(CardRequest request) {
        Card card = Card.builder()
                .userId(request.getUserId())
                .cardHolder(request.getCardHolder())
                .lastFourDigits(request.getLastFourDigits())
                .expiryMonth(request.getExpiryMonth())
                .expiryYear(request.getExpiryYear())
                .build();
        return cardRepository.save(card);
    }

    @Transactional
    public void delete(Long id) {

        transactionRepository.nullifyCard(id);
        cardRepository.deleteById(id);
    }
}
