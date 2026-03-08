package com.example.catalogservice.repository;

import com.example.catalogservice.model.Extra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ExtraRepository extends JpaRepository<Extra, Long> {

    @Query("SELECT DISTINCT e FROM Extra e LEFT JOIN FETCH e.allergens")
    List<Extra> findAllWithAllergens();

    @Query("SELECT DISTINCT e FROM Extra e LEFT JOIN FETCH e.allergens WHERE e.id = :id")
    Optional<Extra> findByIdWithAllergens(@Param("id") Long id);
}
