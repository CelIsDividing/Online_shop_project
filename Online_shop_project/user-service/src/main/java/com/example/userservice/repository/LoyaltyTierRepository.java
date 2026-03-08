package com.example.userservice.repository;

import com.example.userservice.model.LoyaltyTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface LoyaltyTierRepository extends JpaRepository<LoyaltyTier, Long> {

    Optional<LoyaltyTier> findByName(String name);

    @Query("SELECT t FROM LoyaltyTier t ORDER BY t.minPoints ASC")
    List<LoyaltyTier> findAllOrderedByMinPoints();
}
