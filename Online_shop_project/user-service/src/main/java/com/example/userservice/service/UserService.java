package com.example.userservice.service;

import com.example.userservice.dto.RegisterUserRequest;
import com.example.userservice.exception.EmailAlreadyExistsException;
import com.example.userservice.model.LoyaltyAccount;
import com.example.userservice.model.LoyaltyTier;
import com.example.userservice.model.User;
import com.example.userservice.repository.LoyaltyAccountRepository;
import com.example.userservice.repository.LoyaltyTierRepository;
import com.example.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final LoyaltyAccountRepository loyaltyAccountRepository;
    private final LoyaltyTierRepository loyaltyTierRepository;
    private final NotificationService notificationService;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findByIdWithLoyalty(id);
    }

    @Transactional
    public User register(RegisterUserRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }
        User user = User.builder()
                .name(request.getName().trim())
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null ? request.getRole().toUpperCase() : "KUPAC")
                .build();
        user = userRepository.save(user);

        LoyaltyTier bronzeTier = loyaltyTierRepository.findByName("BRONZE")
                .orElseGet(() -> loyaltyTierRepository.findAllOrderedByMinPoints().stream().findFirst()
                        .orElseThrow(() -> new IllegalStateException("Nema definisanih loyalty tier-ova.")));

        LoyaltyAccount loyalty = LoyaltyAccount.builder()
                .userId(user.getId())
                .user(user)
                .points(0)
                .loyaltyTier(bronzeTier)
                .build();
        loyaltyAccountRepository.save(loyalty);
        user.setLoyaltyAccount(loyalty);
        return userRepository.save(user);
    }

    @Transactional
    public User update(Long id, RegisterUserRequest request) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) return null;
        String email = request.getEmail().trim().toLowerCase();
        if (!email.equals(user.getEmail()) && userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }
        user.setName(request.getName().trim());
        user.setEmail(email);
        return userRepository.save(user);
    }

    @Transactional
    public Optional<LoyaltyAccount> addPointsForPurchase(Long userId, BigDecimal purchaseAmount) {
        return loyaltyAccountRepository.findByUserId(userId).map(loyalty -> {
            int earnedPoints = purchaseAmount
                    .multiply(BigDecimal.valueOf(0.10))
                    .setScale(0, RoundingMode.FLOOR)
                    .intValue();
            int newTotal = loyalty.getPoints() + earnedPoints;
            loyalty.setPoints(newTotal);

            String oldTierName = loyalty.getLoyaltyTier().getName();

            List<LoyaltyTier> allTiers = loyaltyTierRepository.findAllOrderedByMinPoints();
            LoyaltyTier bestTier = allTiers.get(0);
            for (LoyaltyTier tier : allTiers) {
                if (newTotal >= tier.getMinPoints()) {
                    bestTier = tier;
                }
            }
            loyalty.setLoyaltyTier(bestTier);
            LoyaltyAccount saved = loyaltyAccountRepository.save(loyalty);

            List<NotificationService.NotificationEntry> pending = new ArrayList<>();

            if (earnedPoints > 0) {
                pending.add(new NotificationService.NotificationEntry(
                        "Zaradili ste " + earnedPoints + " loyalty poena! Ukupno poena: " + newTotal + ".",
                        "POINTS_EARNED"));
            }

            if (!bestTier.getName().equals(oldTierName)) {
                pending.add(new NotificationService.NotificationEntry(
                        "Čestitamo! Vaš loyalty nivo je unapređen: " + oldTierName + " → " + bestTier.getName() +
                                " (popust " + bestTier.getDiscountPercent() + "%).",
                        "TIER_UPGRADE"));
            }

            if (!pending.isEmpty()) {
                notificationService.createBatchForUser(userId, pending);
            }

            return saved;
        });
    }

    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }

    @Transactional
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Optional<User> validateLogin(String email, String password) {
        if (password == null) return Optional.empty();
        return userRepository.findByEmail(email.trim().toLowerCase())
                .filter(u -> passwordEncoder.matches(password, u.getPassword()));
    }
}
