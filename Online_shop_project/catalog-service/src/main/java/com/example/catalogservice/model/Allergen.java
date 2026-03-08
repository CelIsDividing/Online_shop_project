package com.example.catalogservice.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "allergens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Allergen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 500)
    private String description;
}
