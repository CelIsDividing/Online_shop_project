package com.example.apigateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Validates every request through the API Gateway:
 *  - Public path (login): pass through freely.
 *  - X-API-Key header (web-app internal Feign calls): pass through, mark as internal.
 *  - Authorization: Bearer <jwt>: validate, inject X-User-Id + X-User-Role headers.
 *  - Everything else: 401 Unauthorized.
 */
@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private static final List<String> PUBLIC_PATHS = List.of(
            "/user-service/api/users/login",
            "/**/health"
    );

    private final String apiKey;
    private final String jwtSecret;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public JwtAuthFilter(GatewaySecurityProperties props,
                         @Value("${jwt.secret}") String jwtSecret) {
        this.apiKey = props.getApiKey();
        this.jwtSecret = jwtSecret;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        String apiKeyHeader = exchange.getRequest().getHeaders().getFirst("X-API-Key");
        if (apiKey != null && apiKey.equals(apiKeyHeader)) {
            ServerWebExchange mutated = exchange.mutate()
                    .request(exchange.getRequest().mutate()
                            .header("X-Internal", "true")
                            .build())
                    .build();
            return chain.filter(mutated);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Claims claims = parseToken(token);
                String userId = claims.getSubject();
                String role   = claims.get("role", String.class);

                ServerWebExchange mutated = exchange.mutate()
                        .request(exchange.getRequest().mutate()
                                .header("X-User-Id",   userId)
                                .header("X-User-Role", role)
                                .build())
                        .build();
                return chain.filter(mutated);
            } catch (Exception e) {
                return unauthorized(exchange, "Invalid or expired token");
            }
        }

        return unauthorized(exchange, "Authentication required");
    }

    private Claims parseToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean isPublicPath(String path) {
        for (String pattern : PUBLIC_PATHS) {
            if (pathMatcher.match(pattern, path)) return true;
        }
        return false;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String reason) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("X-Auth-Error", reason);
        return response.setComplete();
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
