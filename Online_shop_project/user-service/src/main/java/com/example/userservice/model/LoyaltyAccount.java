package com.example.userservice.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "loyalty_accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = "user")
@ToString(exclude = "user")
public class LoyaltyAccount {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private User user;

    @Column(nullable = false)
    @Builder.Default
    private Integer points = 0;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "loyalty_tier_id", nullable = false)
    private LoyaltyTier loyaltyTier;
}
