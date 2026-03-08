package com.example.webapp.client;

import com.example.webapp.client.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "catalog-service", url = "${catalog-service.url:http://localhost:8765/catalog-service}", configuration = FeignClientConfig.class)
public interface CatalogServiceClient {

    // ---- Products ----
    @GetMapping("/api/products")
    List<ProductDto> getAllProducts();

    @GetMapping("/api/products/{id}")
    ProductDto getProductById(@PathVariable Long id);

    @PostMapping("/api/products")
    ProductDto createProduct(@RequestBody ProductRequestDto request);

    @PutMapping("/api/products/{id}")
    ProductDto updateProduct(@PathVariable Long id, @RequestBody ProductRequestDto request);

    @DeleteMapping("/api/products/{id}")
    void deleteProduct(@PathVariable Long id);

    // ---- Extras ----
    @GetMapping("/api/extras")
    List<ExtraDto> getAllExtras();

    @GetMapping("/api/extras/{id}")
    ExtraDto getExtraById(@PathVariable Long id);

    @PostMapping("/api/extras")
    ExtraDto createExtra(@RequestBody ExtraRequestDto request);

    @PutMapping("/api/extras/{id}")
    ExtraDto updateExtra(@PathVariable Long id, @RequestBody ExtraRequestDto request);

    @DeleteMapping("/api/extras/{id}")
    void deleteExtra(@PathVariable Long id);

    // ---- Allergens ----
    @GetMapping("/api/allergens")
    List<AllergenDto> getAllAllergens();

    @PostMapping("/api/allergens")
    AllergenDto createAllergen(@RequestBody AllergenRequestDto request);

    @PutMapping("/api/allergens/{id}")
    AllergenDto updateAllergen(@PathVariable Long id, @RequestBody AllergenRequestDto request);

    @DeleteMapping("/api/allergens/{id}")
    void deleteAllergen(@PathVariable Long id);
}
