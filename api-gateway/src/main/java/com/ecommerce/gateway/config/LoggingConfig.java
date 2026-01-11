package com.ecommerce.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Configuration
@Slf4j
public class LoggingConfig {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public GlobalFilter loggingFilter() {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            
            // Générer ou récupérer le Correlation ID
            String correlationId = request.getHeaders().getFirst("X-Correlation-ID");
            if (correlationId == null) {
                correlationId = UUID.randomUUID().toString();
            }
            
            final String finalCorrelationId = correlationId;
            long startTime = System.currentTimeMillis();

            // Ajouter le Correlation ID à la requête
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-Correlation-ID", finalCorrelationId)
                    .build();

            log.info("[{}] Requête entrante: {} {} depuis {}",
                    finalCorrelationId,
                    request.getMethod(),
                    request.getURI().getPath(),
                    request.getRemoteAddress());

            return chain.filter(exchange.mutate().request(mutatedRequest).build())
                    .then(Mono.fromRunnable(() -> {
                        ServerHttpResponse response = exchange.getResponse();
                        long duration = System.currentTimeMillis() - startTime;
                        
                        // Ajouter le Correlation ID à la réponse
                        response.getHeaders().add("X-Correlation-ID", finalCorrelationId);
                        
                        log.info("[{}] Réponse: {} {} - Status: {} - Durée: {}ms",
                                finalCorrelationId,
                                request.getMethod(),
                                request.getURI().getPath(),
                                response.getStatusCode(),
                                duration);
                    }));
        };
    }
}
