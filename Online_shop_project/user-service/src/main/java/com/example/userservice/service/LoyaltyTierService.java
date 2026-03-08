package com.example.userservice.service;

import com.example.userservice.dto.LoyaltyTierRequest;
import com.example.userservice.model.LoyaltyTier;
import com.example.userservice.repository.LoyaltyTierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoyaltyTierService {

    private final LoyaltyTierRepository loyaltyTierRepository;

    @Transactional(readOnly = true)
    public List<LoyaltyTier> findAll() {
        return loyaltyTierRepository.findAllOrderedByMinPoints();
    }

    @Transactional(readOnly = true)
    public Optional<LoyaltyTier> findById(Long id) {
        return loyaltyTierRepository.findById(id);
    }

    @Transactional
    public LoyaltyTier create(LoyaltyTierRequest request) {
        LoyaltyTier tier = LoyaltyTier.builder()
                .name(request.getName().toUpperCase())
                .discountPercent(request.getDiscountPercent())
                .minPoints(request.getMinPoints())
                .build();
        return loyaltyTierRepository.save(tier);
    }

    @Transactional
    public Optional<LoyaltyTier> update(Long id, LoyaltyTierRequest request) {
        return loyaltyTierRepository.findById(id).map(tier -> {
            tier.setName(request.getName().toUpperCase());
            tier.setDiscountPercent(request.getDiscountPercent());
            tier.setMinPoints(request.getMinPoints());
            return loyaltyTierRepository.save(tier);
        });
    }

    @Transactional
    public void delete(Long id) {
        loyaltyTierRepository.deleteById(id);
    }
}
