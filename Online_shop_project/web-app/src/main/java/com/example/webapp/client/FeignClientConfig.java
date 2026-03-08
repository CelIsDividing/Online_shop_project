package com.example.webapp.client;

import com.example.webapp.controller.LoginController;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpSession;

/**
 * Attaches an Authorization header (JWT Bearer token) to all outgoing Feign requests.
 * Falls back to X-API-Key when no JWT is present in the session (e.g. during startup probes).
 */
@Configuration
public class FeignClientConfig {

    @Value("${gateway.api-key:gateway-api-key}")
    private String apiKey;

    @Bean
    public RequestInterceptor authRequestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(@NonNull RequestTemplate template) {
                try {
                    ServletRequestAttributes attrs =
                            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                    if (attrs != null) {
                        HttpSession session = attrs.getRequest().getSession(false);
                        if (session != null) {
                            String token = (String) session.getAttribute(LoginController.SESSION_JWT_TOKEN);
                            if (token != null && !token.isBlank()) {
                                template.header("Authorization", "Bearer " + token);
                                return;
                            }
                        }
                    }
                } catch (Exception ignored) {}
                template.header("X-API-Key", apiKey);
            }
        };
    }
}
