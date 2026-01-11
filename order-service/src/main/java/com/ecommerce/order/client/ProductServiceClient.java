package com.ecommerce.order.client;

import com.ecommerce.order.dto.StockCheckResponse;
import com.ecommerce.order.exception.ProductServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class ProductServiceClient {

    private final WebClient webClient;

    public ProductServiceClient(
            WebClient.Builder webClientBuilder,
            @Value("${product.service.url:http://localhost:8081}") String productServiceUrl) {
        this.webClient = webClientBuilder
                .baseUrl(productServiceUrl)
                .build();
    }

    /**
     * Vérifie le stock pour un produit
     */
    public StockCheckResponse checkStock(Long productId, Integer quantite, String token) {
        log.info("Vérification du stock pour le produit ID: {}, quantité: {}", productId, quantite);

        Map<String, Object> request = Map.of(
                "productId", productId,
                "quantite", quantite
        );

        return webClient.post()
                .uri("/api/produits/stock/check")
                .header(HttpHeaders.AUTHORIZATION, token)
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> 
                    response.bodyToMono(String.class)
                        .flatMap(body -> Mono.error(new ProductServiceException(
                            "Erreur lors de la vérification du stock: " + body))))
                .bodyToMono(StockCheckResponse.class)
                .block();
    }

    /**
     * Vérifie le stock pour plusieurs produits
     */
    @SuppressWarnings("unchecked")
    public List<StockCheckResponse> checkMultipleStock(List<Map<String, Object>> requests, String token) {
        log.info("Vérification du stock pour {} produits", requests.size());

        return webClient.post()
                .uri("/api/produits/stock/check-multiple")
                .header(HttpHeaders.AUTHORIZATION, token)
                .bodyValue(requests)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> 
                    response.bodyToMono(String.class)
                        .flatMap(body -> Mono.error(new ProductServiceException(
                            "Erreur lors de la vérification du stock: " + body))))
                .bodyToFlux(StockCheckResponse.class)
                .collectList()
                .block();
    }

    /**
     * Décrémente le stock d'un produit
     */
    public boolean decrementStock(Long productId, Integer quantite, String token) {
        log.info("Décrémentation du stock pour le produit ID: {}, quantité: {}", productId, quantite);

        Map<String, Object> request = Map.of(
                "productId", productId,
                "quantite", quantite
        );

        Boolean result = webClient.post()
                .uri("/api/produits/stock/decrement")
                .header(HttpHeaders.AUTHORIZATION, token)
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> 
                    response.bodyToMono(String.class)
                        .flatMap(body -> Mono.error(new ProductServiceException(
                            "Erreur lors de la décrémentation du stock: " + body))))
                .bodyToMono(Boolean.class)
                .block();

        return Boolean.TRUE.equals(result);
    }

    /**
     * Incrémente le stock d'un produit (rollback)
     */
    public boolean incrementStock(Long productId, Integer quantite, String token) {
        log.info("Incrémentation du stock pour le produit ID: {}, quantité: {}", productId, quantite);

        Map<String, Object> request = Map.of(
                "productId", productId,
                "quantite", quantite
        );

        Boolean result = webClient.post()
                .uri("/api/produits/stock/increment")
                .header(HttpHeaders.AUTHORIZATION, token)
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> 
                    response.bodyToMono(String.class)
                        .flatMap(body -> Mono.error(new ProductServiceException(
                            "Erreur lors de l'incrémentation du stock: " + body))))
                .bodyToMono(Boolean.class)
                .block();

        return Boolean.TRUE.equals(result);
    }
}
