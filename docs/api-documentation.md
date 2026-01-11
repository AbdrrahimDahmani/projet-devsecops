# Documentation Technique API

## API Produits

Base URL: `http://localhost:8080/api/produits`

### Endpoints

#### Lister tous les produits
```http
GET /api/produits
Authorization: Bearer <token>
```

**Réponse:**
```json
[
  {
    "id": 1,
    "nom": "Laptop HP ProBook",
    "description": "Ordinateur portable professionnel",
    "prix": 899.99,
    "quantiteStock": 50,
    "actif": true
  }
]
```

**Codes de réponse:**
- `200 OK`: Liste retournée avec succès
- `401 Unauthorized`: Token invalide ou expiré
- `403 Forbidden`: Rôle insuffisant

---

#### Obtenir un produit par ID
```http
GET /api/produits/{id}
Authorization: Bearer <token>
```

**Réponse:**
```json
{
  "id": 1,
  "nom": "Laptop HP ProBook",
  "description": "Ordinateur portable professionnel",
  "prix": 899.99,
  "quantiteStock": 50,
  "actif": true
}
```

**Codes de réponse:**
- `200 OK`: Produit trouvé
- `404 Not Found`: Produit inexistant

---

#### Créer un produit (ADMIN)
```http
POST /api/produits
Authorization: Bearer <admin_token>
Content-Type: application/json

{
  "nom": "Nouveau Produit",
  "description": "Description du produit",
  "prix": 99.99,
  "quantiteStock": 100
}
```

**Réponse:**
```json
{
  "id": 11,
  "nom": "Nouveau Produit",
  "description": "Description du produit",
  "prix": 99.99,
  "quantiteStock": 100,
  "actif": true
}
```

**Codes de réponse:**
- `201 Created`: Produit créé
- `400 Bad Request`: Données invalides
- `403 Forbidden`: Rôle ADMIN requis

---

#### Modifier un produit (ADMIN)
```http
PUT /api/produits/{id}
Authorization: Bearer <admin_token>
Content-Type: application/json

{
  "nom": "Produit Modifié",
  "description": "Nouvelle description",
  "prix": 149.99,
  "quantiteStock": 75
}
```

---

#### Supprimer un produit (ADMIN)
```http
DELETE /api/produits/{id}
Authorization: Bearer <admin_token>
```

**Codes de réponse:**
- `204 No Content`: Produit supprimé (soft delete)
- `404 Not Found`: Produit inexistant

---

#### Rechercher des produits
```http
GET /api/produits/search?keyword=laptop
Authorization: Bearer <token>
```

---

#### Vérifier le stock
```http
POST /api/produits/stock/check
Authorization: Bearer <token>
Content-Type: application/json

{
  "productId": 1,
  "quantite": 5
}
```

**Réponse:**
```json
{
  "productId": 1,
  "nomProduit": "Laptop HP ProBook",
  "prixUnitaire": 899.99,
  "quantiteDemandee": 5,
  "quantiteDisponible": 50,
  "disponible": true,
  "message": "Stock suffisant"
}
```

---

## API Commandes

Base URL: `http://localhost:8080/api/commandes`

### Endpoints

#### Créer une commande (CLIENT)
```http
POST /api/commandes
Authorization: Bearer <client_token>
Content-Type: application/json

{
  "items": [
    {"productId": 1, "quantite": 2},
    {"productId": 3, "quantite": 1}
  ]
}
```

**Réponse:**
```json
{
  "id": 1,
  "userId": "user-uuid",
  "username": "client",
  "dateCommande": "2026-01-09T10:30:00",
  "statut": "CONFIRMEE",
  "statutLabel": "Confirmée",
  "montantTotal": 1879.97,
  "items": [
    {
      "id": 1,
      "productId": 1,
      "nomProduit": "Laptop HP ProBook",
      "quantite": 2,
      "prixUnitaire": 899.99,
      "sousTotal": 1799.98
    },
    {
      "id": 2,
      "productId": 3,
      "nomProduit": "Clavier Mécanique",
      "quantite": 1,
      "prixUnitaire": 79.99,
      "sousTotal": 79.99
    }
  ]
}
```

**Codes de réponse:**
- `201 Created`: Commande créée
- `400 Bad Request`: Stock insuffisant ou données invalides
- `403 Forbidden`: Rôle CLIENT requis

---

#### Mes commandes (CLIENT)
```http
GET /api/commandes/mes-commandes
Authorization: Bearer <client_token>
```

---

#### Toutes les commandes (ADMIN)
```http
GET /api/commandes
Authorization: Bearer <admin_token>
```

---

#### Détail d'une commande
```http
GET /api/commandes/{id}
Authorization: Bearer <token>
```

---

#### Annuler une commande
```http
POST /api/commandes/{id}/annuler
Authorization: Bearer <token>
```

**Codes de réponse:**
- `200 OK`: Commande annulée
- `400 Bad Request`: Commande non annulable (déjà livrée/expédiée)
- `403 Forbidden`: Non autorisé à annuler cette commande
- `404 Not Found`: Commande inexistante

---

## Modèle de Données

### Produit
| Champ | Type | Description |
|-------|------|-------------|
| id | Long | Identifiant unique |
| nom | String | Nom du produit (2-100 caractères) |
| description | String | Description (max 500 caractères) |
| prix | BigDecimal | Prix unitaire (> 0) |
| quantiteStock | Integer | Quantité en stock (≥ 0) |
| actif | Boolean | Produit actif/supprimé |

### Commande
| Champ | Type | Description |
|-------|------|-------------|
| id | Long | Identifiant unique |
| userId | String | UUID de l'utilisateur Keycloak |
| username | String | Nom d'utilisateur |
| dateCommande | LocalDateTime | Date de création |
| statut | OrderStatus | État de la commande |
| montantTotal | BigDecimal | Montant total calculé |
| items | List<OrderItem> | Produits commandés |

### OrderStatus
| Valeur | Description |
|--------|-------------|
| EN_ATTENTE | Commande en attente de traitement |
| CONFIRMEE | Commande confirmée |
| EN_PREPARATION | Commande en préparation |
| EXPEDIEE | Commande expédiée |
| LIVREE | Commande livrée |
| ANNULEE | Commande annulée |

---

## Gestion des Erreurs

### Format de réponse d'erreur
```json
{
  "timestamp": "2026-01-09T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Stock insuffisant pour le produit Laptop HP ProBook"
}
```

### Erreurs de validation
```json
{
  "timestamp": "2026-01-09T10:30:00",
  "status": 400,
  "error": "Validation Failed",
  "message": "Erreurs de validation",
  "validationErrors": {
    "nom": "Le nom est obligatoire",
    "prix": "Le prix doit être supérieur à 0"
  }
}
```

---

## Authentification

### Obtenir un token
```bash
curl -X POST "http://localhost:8180/realms/ecommerce/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=ecommerce-frontend" \
  -d "username=client" \
  -d "password=client123"
```

### Utiliser le token
```bash
curl -X GET "http://localhost:8080/api/produits" \
  -H "Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI..."
```

### Structure du JWT
```json
{
  "sub": "user-uuid",
  "preferred_username": "client",
  "realm_access": {
    "roles": ["CLIENT"]
  },
  "exp": 1736420000
}
```
