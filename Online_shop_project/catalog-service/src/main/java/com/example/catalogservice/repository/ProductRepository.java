package com.example.catalogservice.repository;

import com.example.catalogservice.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.allergens")
    List<Product> findAllWithAllergens();

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.allergens WHERE p.id = :id")
    Optional<Product> findByIdWithAllergens(@Param("id") Long id);
}
