package com.example.catalogservice.controller;

import com.example.catalogservice.dto.AllergenRequest;
import com.example.catalogservice.model.Allergen;
import com.example.catalogservice.service.AllergenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/allergens")
@RequiredArgsConstructor
public class AllergenController {

    private final AllergenService allergenService;

    @GetMapping
    public List<Allergen> list() {
        return allergenService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Allergen> getById(@PathVariable Long id) {
        return allergenService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Allergen> create(@Valid @RequestBody AllergenRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(allergenService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Allergen> update(@PathVariable Long id, @Valid @RequestBody AllergenRequest request) {
        return allergenService.update(id, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return allergenService.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
