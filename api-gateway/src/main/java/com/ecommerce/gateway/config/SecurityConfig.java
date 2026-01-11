package com.ecommerce.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        // Utilise uniquement jwk-set-uri sans valider l'issuer
        return NimbusReactiveJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeExchange(exchanges -> exchanges
                // Endpoints publics
                .pathMatchers("/actuator/**").permitAll()
                
                // Endpoints Produits
                .pathMatchers(HttpMethod.GET, "/api/produits/**").hasAnyRole("ADMIN", "CLIENT")
                .pathMatchers(HttpMethod.POST, "/api/produits").hasRole("ADMIN")
                .pathMatchers(HttpMethod.PUT, "/api/produits/**").hasRole("ADMIN")
                .pathMatchers(HttpMethod.DELETE, "/api/produits/**").hasRole("ADMIN")
                .pathMatchers(HttpMethod.POST, "/api/produits/stock/**").hasAnyRole("ADMIN", "CLIENT")
                
                // Endpoints Commandes
                .pathMatchers(HttpMethod.POST, "/api/commandes").hasRole("CLIENT")
                .pathMatchers(HttpMethod.GET, "/api/commandes/mes-commandes").hasRole("CLIENT")
                .pathMatchers(HttpMethod.GET, "/api/commandes").hasRole("ADMIN")
                .pathMatchers(HttpMethod.GET, "/api/commandes/**").hasAnyRole("ADMIN", "CLIENT")
                .pathMatchers(HttpMethod.POST, "/api/commandes/*/annuler").hasAnyRole("ADMIN", "CLIENT")
                
                .anyExchange().authenticated())
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(grantedAuthoritiesExtractor())));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://frontend:3000"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private ReactiveJwtAuthenticationConverterAdapter grantedAuthoritiesExtractor() {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new KeycloakRealmRoleConverter());
        return new ReactiveJwtAuthenticationConverterAdapter(jwtAuthenticationConverter);
    }

    public static class KeycloakRealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

        @Override
        @SuppressWarnings("unchecked")
        public Collection<GrantedAuthority> convert(Jwt jwt) {
            System.out.println("=== JWT CONVERTER CALLED ===");
            System.out.println("JWT Claims: " + jwt.getClaims());
            
            Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
            System.out.println("realm_access: " + realmAccess);
            
            if (realmAccess == null || !realmAccess.containsKey("roles")) {
                System.out.println("No roles found, returning empty list");
                return Collections.emptyList();
            }

            List<String> roles = (List<String>) realmAccess.get("roles");
            System.out.println("Roles found: " + roles);
            
            List<GrantedAuthority> authorities = roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                    .collect(Collectors.toList());
            
            System.out.println("Authorities: " + authorities);
            return authorities;
        }
    }
}
