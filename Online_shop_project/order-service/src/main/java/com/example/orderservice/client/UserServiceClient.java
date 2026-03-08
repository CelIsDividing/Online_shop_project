package com.example.orderservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 
 * Order-service proverava da li korisnik postoji pre kreiranja porudžbine.
 */
@FeignClient(name = "user-service", url = "${user-service.url:http://localhost:8003}")
public interface UserServiceClient {

    @GetMapping("/api/users/{id}")
    UserResponse getUserById(@PathVariable Long id);

    record UserResponse(Long id, String name) {}
}
