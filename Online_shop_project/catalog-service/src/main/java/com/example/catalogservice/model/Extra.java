package com.example.catalogservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "extras")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = "allergens")
@ToString(exclude = "allergens")
public class Extra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column
    private Integer calories;

    @ManyToMany
    @JoinTable(
            name = "extra_allergens",
            joinColumns = @JoinColumn(name = "extra_id"),
            inverseJoinColumns = @JoinColumn(name = "allergen_id")
    )
    @Builder.Default
    private Set<Allergen> allergens = new HashSet<>();
}
