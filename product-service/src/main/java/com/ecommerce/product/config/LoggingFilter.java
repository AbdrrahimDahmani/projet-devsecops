package com.ecommerce.product.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Slf4j
public class LoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        String correlationId = request.getHeader("X-Correlation-ID");
        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
        }

        MDC.put("correlationId", correlationId);
        MDC.put("requestMethod", request.getMethod());
        MDC.put("requestUri", request.getRequestURI());

        long startTime = System.currentTimeMillis();

        try {
            // Ajouter l'utilisateur au MDC si authentifié
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
                String username = jwt.getClaimAsString("preferred_username");
                MDC.put("username", username != null ? username : "unknown");
            }

            log.info("Requête entrante: {} {}", request.getMethod(), request.getRequestURI());

            response.setHeader("X-Correlation-ID", correlationId);
            filterChain.doFilter(request, response);

        } finally {
            long duration = System.currentTimeMillis() - startTime;
            MDC.put("responseStatus", String.valueOf(response.getStatus()));
            MDC.put("duration", String.valueOf(duration));

            log.info("Requête traitée: {} {} - Status: {} - Durée: {}ms", 
                    request.getMethod(), 
                    request.getRequestURI(), 
                    response.getStatus(), 
                    duration);

            MDC.clear();
        }
    }
}
