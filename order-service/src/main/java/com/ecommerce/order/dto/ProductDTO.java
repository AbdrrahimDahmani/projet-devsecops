package com.ecommerce.order.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {
    private Long id;
    private String nom;
    private String description;
    private BigDecimal prix;
    private Integer quantiteStock;
    private Boolean actif;
}
