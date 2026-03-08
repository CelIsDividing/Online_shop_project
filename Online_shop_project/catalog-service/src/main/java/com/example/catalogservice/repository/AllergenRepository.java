package com.example.catalogservice.repository;

import com.example.catalogservice.model.Allergen;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AllergenRepository extends JpaRepository<Allergen, Long> {
    boolean existsByName(String name);
}
