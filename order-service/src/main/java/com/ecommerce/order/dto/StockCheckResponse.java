package com.ecommerce.order.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockCheckResponse {
    private Long productId;
    private String nomProduit;
    private BigDecimal prixUnitaire;
    private Integer quantiteDemandee;
    private Integer quantiteDisponible;
    private Boolean disponible;
    private String message;
}
