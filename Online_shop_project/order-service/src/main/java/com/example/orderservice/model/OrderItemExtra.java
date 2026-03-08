package com.example.orderservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_item_extras")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = "orderItem")
@ToString(exclude = "orderItem")
public class OrderItemExtra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    @JsonIgnore
    private OrderItem orderItem;

    @Column(name = "extra_id", nullable = false)
    private Long extraId;

    @Column(name = "extra_name", nullable = false, length = 255)
    private String extraName;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
}
