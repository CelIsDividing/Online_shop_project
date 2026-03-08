package com.example.catalogservice.controller;

import com.example.catalogservice.dto.ExtraRequest;
import com.example.catalogservice.model.Extra;
import com.example.catalogservice.service.ExtraService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/extras")
@RequiredArgsConstructor
public class ExtraController {

    private final ExtraService extraService;

    @GetMapping
    public List<Extra> list() {
        return extraService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Extra> getById(@PathVariable Long id) {
        return extraService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Extra> create(@Valid @RequestBody ExtraRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(extraService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Extra> update(@PathVariable Long id, @Valid @RequestBody ExtraRequest request) {
        return extraService.update(id, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return extraService.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
