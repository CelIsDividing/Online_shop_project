package com.example.catalogservice.service;

import com.example.catalogservice.dto.ExtraRequest;
import com.example.catalogservice.model.Allergen;
import com.example.catalogservice.model.Extra;
import com.example.catalogservice.repository.AllergenRepository;
import com.example.catalogservice.repository.ExtraRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ExtraService {

    private final ExtraRepository extraRepository;
    private final AllergenRepository allergenRepository;

    @Transactional(readOnly = true)
    public List<Extra> findAll() {
        return extraRepository.findAllWithAllergens();
    }

    @Transactional(readOnly = true)
    public Optional<Extra> findById(Long id) {
        return extraRepository.findByIdWithAllergens(id);
    }

    @Transactional
    public Extra create(ExtraRequest request) {
        Set<Allergen> allergens = resolveAllergens(request.getAllergenIds());
        Extra extra = Extra.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .price(request.getPrice())
                .calories(request.getCalories())
                .allergens(allergens)
                .build();
        return extraRepository.save(extra);
    }

    @Transactional
    public Optional<Extra> update(Long id, ExtraRequest request) {
        return extraRepository.findById(id).map(e -> {
            e.setName(request.getName().trim());
            e.setDescription(request.getDescription());
            e.setPrice(request.getPrice());
            e.setCalories(request.getCalories());
            e.setAllergens(resolveAllergens(request.getAllergenIds()));
            return extraRepository.save(e);
        });
    }

    @Transactional
    public boolean delete(Long id) {
        if (!extraRepository.existsById(id)) return false;
        extraRepository.deleteById(id);
        return true;
    }

    private Set<Allergen> resolveAllergens(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) return new HashSet<>();
        return new HashSet<>(allergenRepository.findAllById(ids));
    }
}
