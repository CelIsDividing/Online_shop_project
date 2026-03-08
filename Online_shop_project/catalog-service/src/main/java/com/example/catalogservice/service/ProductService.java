package com.example.catalogservice.service;

import com.example.catalogservice.dto.ProductRequest;
import com.example.catalogservice.model.Allergen;
import com.example.catalogservice.model.Product;
import com.example.catalogservice.repository.AllergenRepository;
import com.example.catalogservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final AllergenRepository allergenRepository;

    @Transactional(readOnly = true)
    public List<Product> findAll() {
        return productRepository.findAllWithAllergens();
    }

    @Transactional(readOnly = true)
    public Optional<Product> findById(Long id) {
        return productRepository.findByIdWithAllergens(id);
    }

    @Transactional
    public Product create(ProductRequest request) {
        Set<Allergen> allergens = resolveAllergens(request.getAllergenIds());
        Product product = Product.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .price(request.getPrice())
                .size(request.getSize())
                .calories(request.getCalories())
                .allergens(allergens)
                .build();
        return productRepository.save(product);
    }

    @Transactional
    public Optional<Product> update(Long id, ProductRequest request) {
        return productRepository.findById(id).map(p -> {
            p.setName(request.getName().trim());
            p.setDescription(request.getDescription());
            p.setPrice(request.getPrice());
            p.setSize(request.getSize());
            p.setCalories(request.getCalories());
            p.setAllergens(resolveAllergens(request.getAllergenIds()));
            return productRepository.save(p);
        });
    }

    @Transactional
    public boolean delete(Long id) {
        if (!productRepository.existsById(id)) return false;
        productRepository.deleteById(id);
        return true;
    }

    private Set<Allergen> resolveAllergens(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) return new HashSet<>();
        return new HashSet<>(allergenRepository.findAllById(ids));
    }
}
