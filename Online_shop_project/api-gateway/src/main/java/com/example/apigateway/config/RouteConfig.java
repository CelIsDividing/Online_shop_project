package com.example.apigateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Upravljanje rutama: definiše kako zahtevi idu na mikroservise.
 * Discovery locator (application.properties) i dalje koristi Eureka za dinamičke rute;
 * ovde možemo dodati dodatne ili override rute po potrebi.
 */
@Configuration
public class RouteConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()

                .build();
    }
}
