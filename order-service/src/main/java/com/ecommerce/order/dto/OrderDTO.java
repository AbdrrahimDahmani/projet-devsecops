package com.ecommerce.order.dto;

import com.ecommerce.order.entity.OrderStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDTO {

    private Long id;
    private String userId;
    private String username;
    private LocalDateTime dateCommande;
    private OrderStatus statut;
    private String statutLabel;
    private BigDecimal montantTotal;
    private List<OrderItemDTO> items;
}
