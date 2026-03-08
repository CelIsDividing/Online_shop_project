package com.example.apigateway.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "gateway.security")
public class GatewaySecurityProperties {

    /**
     * API ključ koji klijenti šalju u headeru X-API-Key za zaštićene rute.
     */
    private String apiKey = "gateway-api-key";

    private List<String> publicPaths = new ArrayList<>(List.of(
            "/user-service/api/users/login",
            "/user-service/api/users",
            "/user-service/api/users/**",
            "/user-service/api/loyalty-tiers",
            "/user-service/api/loyalty-tiers/**",
            "/user-service/api/notifications",
            "/user-service/api/notifications/**",
            "/user-service/health",
            "/order-service/api/orders",
            "/order-service/api/orders/**",
            "/order-service/health",
            "/payment-service/api/payments/**",
            "/payment-service/health",
            "/catalog-service/api/products",
            "/catalog-service/api/products/**",
            "/catalog-service/api/extras",
            "/catalog-service/api/extras/**",
            "/catalog-service/api/allergens",
            "/catalog-service/api/allergens/**",
            "/catalog-service/health",
            "/**/health"
    ));

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public List<String> getPublicPaths() {
        return publicPaths;
    }

    public void setPublicPaths(List<String> publicPaths) {
        this.publicPaths = publicPaths;
    }

    public void setPublicPaths(String commaSeparated) {
        if (commaSeparated != null && !commaSeparated.isBlank()) {
            this.publicPaths = List.of(commaSeparated.trim().split("\\s*,\\s*"));
        }
    }
}
