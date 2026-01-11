package com.ecommerce.order.controller;

import com.ecommerce.order.dto.CreateOrderRequest;
import com.ecommerce.order.dto.OrderDTO;
import com.ecommerce.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/commandes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Commandes", description = "API de gestion des commandes")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Créer une commande", description = "Crée une nouvelle commande (CLIENT uniquement)")
    public ResponseEntity<OrderDTO> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest httpRequest) {
        
        String userId = jwt.getSubject();
        String username = jwt.getClaimAsString("preferred_username");
        String token = httpRequest.getHeader(HttpHeaders.AUTHORIZATION);
        
        log.info("Utilisateur {} crée une nouvelle commande", username);
        
        OrderDTO order = orderService.createOrder(request, userId, username, token);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping("/mes-commandes")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Mes commandes", description = "Récupère les commandes de l'utilisateur connecté")
    public ResponseEntity<List<OrderDTO>> getMyOrders(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        String username = jwt.getClaimAsString("preferred_username");
        
        log.info("Utilisateur {} récupère ses commandes", username);
        
        return ResponseEntity.ok(orderService.getMyOrders(userId));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Toutes les commandes", description = "Récupère toutes les commandes (ADMIN uniquement)")
    public ResponseEntity<List<OrderDTO>> getAllOrders(@AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getClaimAsString("preferred_username");
        
        log.info("Admin {} récupère toutes les commandes", username);
        
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    @Operation(summary = "Détail commande", description = "Récupère le détail d'une commande")
    public ResponseEntity<OrderDTO> getOrderById(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        
        String userId = jwt.getSubject();
        boolean isAdmin = hasRole(jwt, "ADMIN");
        
        log.info("Récupération de la commande {} par {}", id, jwt.getClaimAsString("preferred_username"));
        
        return ResponseEntity.ok(orderService.getOrderById(id, userId, isAdmin));
    }

    @PostMapping("/{id}/annuler")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    @Operation(summary = "Annuler une commande", description = "Annule une commande")
    public ResponseEntity<OrderDTO> cancelOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest httpRequest) {
        
        String userId = jwt.getSubject();
        boolean isAdmin = hasRole(jwt, "ADMIN");
        String token = httpRequest.getHeader(HttpHeaders.AUTHORIZATION);
        
        log.info("Annulation de la commande {} par {}", id, jwt.getClaimAsString("preferred_username"));
        
        return ResponseEntity.ok(orderService.cancelOrder(id, userId, isAdmin, token));
    }

    @SuppressWarnings("unchecked")
    private boolean hasRole(Jwt jwt, String role) {
        var realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess != null && realmAccess.containsKey("roles")) {
            List<String> roles = (List<String>) realmAccess.get("roles");
            return roles.contains(role);
        }
        return false;
    }
}
