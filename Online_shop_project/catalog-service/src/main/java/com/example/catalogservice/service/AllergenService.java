package com.example.catalogservice.service;

import com.example.catalogservice.dto.AllergenRequest;
import com.example.catalogservice.model.Allergen;
import com.example.catalogservice.repository.AllergenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AllergenService {

    private final AllergenRepository allergenRepository;

    @Transactional(readOnly = true)
    public List<Allergen> findAll() {
        return allergenRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Allergen> findById(Long id) {
        return allergenRepository.findById(id);
    }

    @Transactional
    public Allergen create(AllergenRequest request) {
        Allergen allergen = Allergen.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .build();
        return allergenRepository.save(allergen);
    }

    @Transactional
    public Optional<Allergen> update(Long id, AllergenRequest request) {
        return allergenRepository.findById(id).map(a -> {
            a.setName(request.getName().trim());
            a.setDescription(request.getDescription());
            return allergenRepository.save(a);
        });
    }

    @Transactional
    public boolean delete(Long id) {
        if (!allergenRepository.existsById(id)) return false;
        allergenRepository.deleteById(id);
        return true;
    }
}
