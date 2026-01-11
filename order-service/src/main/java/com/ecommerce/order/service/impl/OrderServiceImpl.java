package com.ecommerce.order.service.impl;

import com.ecommerce.order.client.ProductServiceClient;
import com.ecommerce.order.dto.*;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderItem;
import com.ecommerce.order.entity.OrderStatus;
import com.ecommerce.order.exception.InsufficientStockException;
import com.ecommerce.order.exception.OrderNotFoundException;
import com.ecommerce.order.exception.OrderValidationException;
import com.ecommerce.order.repository.OrderRepository;
import com.ecommerce.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductServiceClient productServiceClient;

    @Override
    public OrderDTO createOrder(CreateOrderRequest request, String userId, String username, String token) {
        log.info("Création d'une commande pour l'utilisateur: {} avec {} produits", 
                username, request.getItems().size());

        // 1. Vérifier le stock pour tous les produits
        List<Map<String, Object>> stockRequests = request.getItems().stream()
                .map(item -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("productId", item.getProductId());
                    map.put("quantite", item.getQuantite());
                    return map;
                })
                .collect(Collectors.toList());

        List<StockCheckResponse> stockResponses = productServiceClient
                .checkMultipleStock(stockRequests, token);

        // 2. Vérifier que tous les produits sont disponibles
        List<String> errors = new ArrayList<>();
        for (StockCheckResponse response : stockResponses) {
            if (!response.getDisponible()) {
                errors.add(response.getMessage() + " (Produit ID: " + response.getProductId() + ")");
            }
        }

        if (!errors.isEmpty()) {
            throw new InsufficientStockException("Stock insuffisant: " + String.join(", ", errors));
        }

        // 3. Créer la commande
        Order order = Order.builder()
                .userId(userId)
                .username(username)
                .statut(OrderStatus.EN_ATTENTE)
                .montantTotal(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .build();

        // 4. Ajouter les items avec les prix récupérés
        for (StockCheckResponse stockResponse : stockResponses) {
            OrderItem item = OrderItem.builder()
                    .productId(stockResponse.getProductId())
                    .nomProduit(stockResponse.getNomProduit())
                    .quantite(stockResponse.getQuantiteDemandee())
                    .prixUnitaire(stockResponse.getPrixUnitaire())
                    .build();
            order.addItem(item);
        }

        // 5. Calculer le montant total
        order.calculateTotal();

        // 6. Décrémenter le stock pour chaque produit
        List<OrderItem> decrementedItems = new ArrayList<>();
        try {
            for (OrderItem item : order.getItems()) {
                boolean success = productServiceClient.decrementStock(
                        item.getProductId(), 
                        item.getQuantite(), 
                        token);
                if (!success) {
                    throw new InsufficientStockException(
                            "Impossible de réserver le stock pour le produit: " + item.getNomProduit());
                }
                decrementedItems.add(item);
            }
        } catch (Exception e) {
            // Rollback: Restaurer le stock pour les produits déjà décrémentés
            log.error("Erreur lors de la réservation du stock, rollback en cours...", e);
            for (OrderItem item : decrementedItems) {
                try {
                    productServiceClient.incrementStock(item.getProductId(), item.getQuantite(), token);
                } catch (Exception rollbackEx) {
                    log.error("Erreur lors du rollback du stock pour le produit: {}", 
                            item.getProductId(), rollbackEx);
                }
            }
            throw e;
        }

        // 7. Confirmer la commande
        order.setStatut(OrderStatus.CONFIRMEE);

        // 8. Sauvegarder la commande
        Order savedOrder = orderRepository.save(order);
        log.info("Commande créée avec succès - ID: {}, Montant: {}", 
                savedOrder.getId(), savedOrder.getMontantTotal());

        return mapToDTO(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDTO getOrderById(Long orderId, String userId, boolean isAdmin) {
        log.info("Récupération de la commande ID: {} par l'utilisateur: {}", orderId, userId);

        Order order;
        if (isAdmin) {
            order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new OrderNotFoundException("Commande non trouvée: " + orderId));
        } else {
            order = orderRepository.findByIdAndUserId(orderId, userId)
                    .orElseThrow(() -> new OrderNotFoundException(
                            "Commande non trouvée ou vous n'avez pas les droits pour y accéder"));
        }

        return mapToDTO(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDTO> getMyOrders(String userId) {
        log.info("Récupération des commandes de l'utilisateur: {}", userId);
        return orderRepository.findByUserIdOrderByDateCommandeDesc(userId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDTO> getAllOrders() {
        log.info("Récupération de toutes les commandes");
        return orderRepository.findAllOrderByDateDesc()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public OrderDTO cancelOrder(Long orderId, String userId, boolean isAdmin, String token) {
        log.info("Annulation de la commande ID: {} par l'utilisateur: {}", orderId, userId);

        Order order;
        if (isAdmin) {
            order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new OrderNotFoundException("Commande non trouvée: " + orderId));
        } else {
            order = orderRepository.findByIdAndUserId(orderId, userId)
                    .orElseThrow(() -> new OrderNotFoundException(
                            "Commande non trouvée ou vous n'avez pas les droits pour y accéder"));
        }

        // Vérifier que la commande peut être annulée
        if (order.getStatut() == OrderStatus.ANNULEE) {
            throw new OrderValidationException("La commande est déjà annulée");
        }
        if (order.getStatut() == OrderStatus.LIVREE) {
            throw new OrderValidationException("Une commande livrée ne peut pas être annulée");
        }
        if (order.getStatut() == OrderStatus.EXPEDIEE) {
            throw new OrderValidationException("Une commande expédiée ne peut pas être annulée");
        }

        // Restaurer le stock
        for (OrderItem item : order.getItems()) {
            try {
                productServiceClient.incrementStock(item.getProductId(), item.getQuantite(), token);
            } catch (Exception e) {
                log.error("Erreur lors de la restauration du stock pour le produit: {}", 
                        item.getProductId(), e);
            }
        }

        order.setStatut(OrderStatus.ANNULEE);
        Order savedOrder = orderRepository.save(order);
        log.info("Commande annulée avec succès - ID: {}", savedOrder.getId());

        return mapToDTO(savedOrder);
    }

    private OrderDTO mapToDTO(Order order) {
        List<OrderItemDTO> itemDTOs = order.getItems().stream()
                .map(item -> OrderItemDTO.builder()
                        .id(item.getId())
                        .productId(item.getProductId())
                        .nomProduit(item.getNomProduit())
                        .quantite(item.getQuantite())
                        .prixUnitaire(item.getPrixUnitaire())
                        .sousTotal(item.getSousTotal())
                        .build())
                .collect(Collectors.toList());

        return OrderDTO.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .username(order.getUsername())
                .dateCommande(order.getDateCommande())
                .statut(order.getStatut())
                .statutLabel(order.getStatut().getLabel())
                .montantTotal(order.getMontantTotal())
                .items(itemDTOs)
                .build();
    }
}
