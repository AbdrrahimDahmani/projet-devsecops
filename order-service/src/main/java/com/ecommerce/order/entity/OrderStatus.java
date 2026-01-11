package com.ecommerce.order.entity;

public enum OrderStatus {
    EN_ATTENTE("En attente"),
    CONFIRMEE("Confirmée"),
    EN_PREPARATION("En préparation"),
    EXPEDIEE("Expédiée"),
    LIVREE("Livrée"),
    ANNULEE("Annulée");

    private final String label;

    OrderStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
