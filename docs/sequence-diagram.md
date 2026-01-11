# Diagramme de Séquence - Processus de Commande

## Création d'une Commande (Scénario Principal)

```
┌──────────┐     ┌───────────┐     ┌─────────────┐     ┌─────────────┐     ┌─────────────┐     ┌──────────┐
│  Client  │     │  Frontend │     │ API Gateway │     │ Order Svc   │     │Product Svc  │     │Order DB  │
└────┬─────┘     └─────┬─────┘     └──────┬──────┘     └──────┬──────┘     └──────┬──────┘     └────┬─────┘
     │                 │                  │                   │                   │                 │
     │ 1. Sélection    │                  │                   │                   │                 │
     │    produits     │                  │                   │                   │                 │
     │────────────────>│                  │                   │                   │                 │
     │                 │                  │                   │                   │                 │
     │ 2. Validation   │                  │                   │                   │                 │
     │    panier       │                  │                   │                   │                 │
     │────────────────>│                  │                   │                   │                 │
     │                 │                  │                   │                   │                 │
     │                 │ 3. POST /api/commandes               │                   │                 │
     │                 │    {items: [...], JWT Token}         │                   │                 │
     │                 │─────────────────>│                   │                   │                 │
     │                 │                  │                   │                   │                 │
     │                 │                  │ 4. Valider JWT    │                   │                 │
     │                 │                  │    Vérifier rôle  │                   │                 │
     │                 │                  │    CLIENT         │                   │                 │
     │                 │                  │                   │                   │                 │
     │                 │                  │ 5. Forward Request│                   │                 │
     │                 │                  │   (avec JWT)      │                   │                 │
     │                 │                  │──────────────────>│                   │                 │
     │                 │                  │                   │                   │                 │
     │                 │                  │                   │ 6. POST /api/produits/stock/check-multiple
     │                 │                  │                   │   [{productId, quantite}, ...]      │
     │                 │                  │                   │──────────────────>│                 │
     │                 │                  │                   │                   │                 │
     │                 │                  │                   │                   │ 7. Vérifier     │
     │                 │                  │                   │                   │    disponibilité│
     │                 │                  │                   │                   │    en base      │
     │                 │                  │                   │                   │                 │
     │                 │                  │                   │ 8. Réponse: Stock OK                │
     │                 │                  │                   │   [{productId, nomProduit,          │
     │                 │                  │                   │     prixUnitaire, disponible: true}]│
     │                 │                  │                   │<──────────────────│                 │
     │                 │                  │                   │                   │                 │
     │                 │                  │                   │ 9. Pour chaque produit:             │
     │                 │                  │                   │    POST /stock/decrement            │
     │                 │                  │                   │──────────────────>│                 │
     │                 │                  │                   │                   │                 │
     │                 │                  │                   │ 10. Stock décrémenté                │
     │                 │                  │                   │<──────────────────│                 │
     │                 │                  │                   │                   │                 │
     │                 │                  │                   │ 11. Calculer      │                 │
     │                 │                  │                   │     montant total │                 │
     │                 │                  │                   │     (∑ prix × qté)│                 │
     │                 │                  │                   │                   │                 │
     │                 │                  │                   │ 12. Créer commande│                 │
     │                 │                  │                   │    status: CONFIRMEE                │
     │                 │                  │                   │────────────────────────────────────>│
     │                 │                  │                   │                   │                 │
     │                 │                  │                   │ 13. Commande ID   │                 │
     │                 │                  │                   │<────────────────────────────────────│
     │                 │                  │                   │                   │                 │
     │                 │                  │ 14. Response 201  │                   │                 │
     │                 │                  │     Created       │                   │                 │
     │                 │                  │<──────────────────│                   │                 │
     │                 │                  │                   │                   │                 │
     │                 │ 15. Commande     │                   │                   │                 │
     │                 │     créée        │                   │                   │                 │
     │                 │<─────────────────│                   │                   │                 │
     │                 │                  │                   │                   │                 │
     │ 16. Confirmation│                  │                   │                   │                 │
     │     + Détails   │                  │                   │                   │                 │
     │<────────────────│                  │                   │                   │                 │
     │                 │                  │                   │                   │                 │
```

## Scénario d'Erreur - Stock Insuffisant

```
┌──────────┐     ┌───────────┐     ┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│  Client  │     │  Frontend │     │ API Gateway │     │ Order Svc   │     │Product Svc  │
└────┬─────┘     └─────┬─────┘     └──────┬──────┘     └──────┬──────┘     └──────┬──────┘
     │                 │                  │                   │                   │
     │ Valider panier  │                  │                   │                   │
     │────────────────>│                  │                   │                   │
     │                 │                  │                   │                   │
     │                 │ POST /api/commandes                  │                   │
     │                 │─────────────────>│                   │                   │
     │                 │                  │                   │                   │
     │                 │                  │──────────────────>│                   │
     │                 │                  │                   │                   │
     │                 │                  │                   │ Check stock       │
     │                 │                  │                   │──────────────────>│
     │                 │                  │                   │                   │
     │                 │                  │                   │ Stock insuffisant │
     │                 │                  │                   │ disponible: false │
     │                 │                  │                   │<──────────────────│
     │                 │                  │                   │                   │
     │                 │                  │ 400 Bad Request   │                   │
     │                 │                  │ "Stock insuffisant│                   │
     │                 │                  │  pour produit X"  │                   │
     │                 │                  │<──────────────────│                   │
     │                 │                  │                   │                   │
     │                 │ Erreur           │                   │                   │
     │                 │<─────────────────│                   │                   │
     │                 │                  │                   │                   │
     │ Message erreur  │                  │                   │                   │
     │ Toast notification                 │                   │                   │
     │<────────────────│                  │                   │                   │
     │                 │                  │                   │                   │
```

## Annulation d'une Commande

```
┌──────────┐     ┌───────────┐     ┌─────────────┐     ┌─────────────┐     ┌─────────────┐     ┌──────────┐
│  Client  │     │  Frontend │     │ API Gateway │     │ Order Svc   │     │Product Svc  │     │Order DB  │
└────┬─────┘     └─────┬─────┘     └──────┬──────┘     └──────┬──────┘     └──────┬──────┘     └────┬─────┘
     │                 │                  │                   │                   │                 │
     │ 1. Click        │                  │                   │                   │                 │
     │    "Annuler"    │                  │                   │                   │                 │
     │────────────────>│                  │                   │                   │                 │
     │                 │                  │                   │                   │                 │
     │                 │ 2. POST /api/commandes/{id}/annuler  │                   │                 │
     │                 │─────────────────>│                   │                   │                 │
     │                 │                  │                   │                   │                 │
     │                 │                  │ 3. Valider JWT    │                   │                 │
     │                 │                  │    + Autorisation │                   │                 │
     │                 │                  │                   │                   │                 │
     │                 │                  │ 4. Forward        │                   │                 │
     │                 │                  │──────────────────>│                   │                 │
     │                 │                  │                   │                   │                 │
     │                 │                  │                   │ 5. Récupérer      │                 │
     │                 │                  │                   │    commande       │                 │
     │                 │                  │                   │─────────────────────────────────────>
     │                 │                  │                   │                   │                 │
     │                 │                  │                   │ 6. Commande data  │                 │
     │                 │                  │                   │<─────────────────────────────────────
     │                 │                  │                   │                   │                 │
     │                 │                  │                   │ 7. Vérifier que   │                 │
     │                 │                  │                   │    annulation     │                 │
     │                 │                  │                   │    est possible   │                 │
     │                 │                  │                   │                   │                 │
     │                 │                  │                   │ 8. Pour chaque item:                │
     │                 │                  │                   │    POST /stock/increment            │
     │                 │                  │                   │    (restaurer stock)                │
     │                 │                  │                   │──────────────────>│                 │
     │                 │                  │                   │                   │                 │
     │                 │                  │                   │ 9. Stock restauré │                 │
     │                 │                  │                   │<──────────────────│                 │
     │                 │                  │                   │                   │                 │
     │                 │                  │                   │ 10. Mettre à jour │                 │
     │                 │                  │                   │     status: ANNULEE                 │
     │                 │                  │                   │─────────────────────────────────────>
     │                 │                  │                   │                   │                 │
     │                 │                  │ 11. Response 200  │                   │                 │
     │                 │                  │     OK            │                   │                 │
     │                 │                  │<──────────────────│                   │                 │
     │                 │                  │                   │                   │                 │
     │                 │ 12. Succès       │                   │                   │                 │
     │                 │<─────────────────│                   │                   │                 │
     │                 │                  │                   │                   │                 │
     │ 13. Confirmation│                  │                   │                   │                 │
     │     annulation  │                  │                   │                   │                 │
     │<────────────────│                  │                   │                   │                 │
     │                 │                  │                   │                   │                 │
```

## Rollback en Cas d'Erreur lors de la Création

```
┌─────────────┐     ┌─────────────┐     ┌──────────┐
│ Order Svc   │     │Product Svc  │     │Order DB  │
└──────┬──────┘     └──────┬──────┘     └────┬─────┘
       │                   │                 │
       │ 1. Décrémentation │                 │
       │    Produit A      │                 │
       │──────────────────>│                 │
       │                   │                 │
       │ 2. OK             │                 │
       │<──────────────────│                 │
       │                   │                 │
       │ 3. Décrémentation │                 │
       │    Produit B      │                 │
       │──────────────────>│                 │
       │                   │                 │
       │ 4. OK             │                 │
       │<──────────────────│                 │
       │                   │                 │
       │ 5. Décrémentation │                 │
       │    Produit C      │                 │
       │──────────────────>│                 │
       │                   │                 │
       │ 6. ERREUR!        │                 │
       │    (Exception)    │                 │
       │<──────────────────│                 │
       │                   │                 │
       │ ═══════════════════════════════════ │
       │ ║     ROLLBACK AUTOMATIQUE       ║  │
       │ ═══════════════════════════════════ │
       │                   │                 │
       │ 7. Incrémenter    │                 │
       │    Produit A      │                 │
       │──────────────────>│                 │
       │                   │                 │
       │ 8. Incrémenter    │                 │
       │    Produit B      │                 │
       │──────────────────>│                 │
       │                   │                 │
       │ 9. Renvoyer       │                 │
       │    erreur client  │                 │
       │                   │                 │
```

## Légende

- **──────>** : Requête synchrone
- **<──────** : Réponse
- **═══════** : Bloc logique / Transaction
- **JWT Token** : Présent dans le header `Authorization: Bearer <token>`
