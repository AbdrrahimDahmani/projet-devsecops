package com.ecommerce.product.controller;

import com.ecommerce.product.dto.ProductDTO;
import com.ecommerce.product.dto.StockCheckRequest;
import com.ecommerce.product.dto.StockCheckResponse;
import com.ecommerce.product.dto.StockUpdateRequest;
import com.ecommerce.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/produits")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Produits", description = "API de gestion des produits")
@SecurityRequirement(name = "bearerAuth")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    @Operation(summary = "Lister tous les produits", description = "Récupère la liste de tous les produits actifs")
    public ResponseEntity<List<ProductDTO>> getAllProducts(@AuthenticationPrincipal Jwt jwt) {
        log.info("Utilisateur {} récupère la liste des produits", getUsernameFromJwt(jwt));
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    @Operation(summary = "Obtenir un produit", description = "Récupère un produit par son identifiant")
    public ResponseEntity<ProductDTO> getProductById(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        log.info("Utilisateur {} récupère le produit ID: {}", getUsernameFromJwt(jwt), id);
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Créer un produit", description = "Crée un nouveau produit (ADMIN uniquement)")
    public ResponseEntity<ProductDTO> createProduct(
            @Valid @RequestBody ProductDTO productDTO,
            @AuthenticationPrincipal Jwt jwt) {
        log.info("Utilisateur {} crée un nouveau produit: {}", getUsernameFromJwt(jwt), productDTO.getNom());
        ProductDTO createdProduct = productService.createProduct(productDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Modifier un produit", description = "Met à jour un produit existant (ADMIN uniquement)")
    public ResponseEntity<ProductDTO> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductDTO productDTO,
            @AuthenticationPrincipal Jwt jwt) {
        log.info("Utilisateur {} met à jour le produit ID: {}", getUsernameFromJwt(jwt), id);
        return ResponseEntity.ok(productService.updateProduct(id, productDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Supprimer un produit", description = "Supprime un produit (soft delete) (ADMIN uniquement)")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        log.info("Utilisateur {} supprime le produit ID: {}", getUsernameFromJwt(jwt), id);
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    @Operation(summary = "Rechercher des produits", description = "Recherche de produits par mot-clé")
    public ResponseEntity<List<ProductDTO>> searchProducts(
            @RequestParam String keyword,
            @AuthenticationPrincipal Jwt jwt) {
        log.info("Utilisateur {} recherche des produits avec: {}", getUsernameFromJwt(jwt), keyword);
        return ResponseEntity.ok(productService.searchProducts(keyword));
    }

    // Endpoints internes pour la communication inter-services
    
    @PostMapping("/stock/check")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    @Operation(summary = "Vérifier le stock", description = "Vérifie la disponibilité du stock pour un produit")
    public ResponseEntity<StockCheckResponse> checkStock(
            @Valid @RequestBody StockCheckRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        log.info("Vérification du stock pour le produit ID: {} par {}", 
                request.getProductId(), getUsernameFromJwt(jwt));
        return ResponseEntity.ok(productService.checkStock(request));
    }

    @PostMapping("/stock/check-multiple")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    @Operation(summary = "Vérifier le stock multiple", description = "Vérifie la disponibilité du stock pour plusieurs produits")
    public ResponseEntity<List<StockCheckResponse>> checkMultipleStock(
            @Valid @RequestBody List<StockCheckRequest> requests,
            @AuthenticationPrincipal Jwt jwt) {
        log.info("Vérification du stock pour {} produits par {}", 
                requests.size(), getUsernameFromJwt(jwt));
        return ResponseEntity.ok(productService.checkMultipleStock(requests));
    }

    @PostMapping("/stock/decrement")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    @Operation(summary = "Décrémenter le stock", description = "Décrémente le stock d'un produit")
    public ResponseEntity<Boolean> decrementStock(
            @Valid @RequestBody StockUpdateRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        log.info("Décrémentation du stock pour le produit ID: {} par {}", 
                request.getProductId(), getUsernameFromJwt(jwt));
        return ResponseEntity.ok(productService.decrementStock(request));
    }

    @PostMapping("/stock/increment")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    @Operation(summary = "Incrémenter le stock", description = "Incrémente le stock d'un produit")
    public ResponseEntity<Boolean> incrementStock(
            @Valid @RequestBody StockUpdateRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        log.info("Incrémentation du stock pour le produit ID: {} par {}", 
                request.getProductId(), getUsernameFromJwt(jwt));
        return ResponseEntity.ok(productService.incrementStock(request));
    }

    private String getUsernameFromJwt(Jwt jwt) {
        return jwt != null ? jwt.getClaimAsString("preferred_username") : "anonymous";
    }
}
