# Configuration Keycloak pour l'application E-Commerce

## Importation automatique du Realm

Le fichier `realm-export.json` contient la configuration complète du realm Keycloak qui sera importée automatiquement au démarrage du conteneur Docker.

## Configuration manuelle (alternative)

Si vous préférez configurer Keycloak manuellement :

### 1. Créer le Realm

1. Connectez-vous à la console d'administration Keycloak : http://localhost:8180/admin
2. Cliquez sur "Create Realm"
3. Nom : `ecommerce`
4. Cliquez sur "Create"

### 2. Créer les Rôles

1. Allez dans "Realm roles" > "Create role"
2. Créez le rôle `ADMIN`
3. Créez le rôle `CLIENT`

### 3. Créer les Utilisateurs

#### Utilisateur Admin
- Username: `admin`
- Email: `admin@ecommerce.com`
- First Name: `Admin`
- Last Name: `User`
- Enabled: `Yes`
- Email Verified: `Yes`
- Credentials: password = `admin123` (Temporary: OFF)
- Role Mappings: `ADMIN`

#### Utilisateur Client
- Username: `client`
- Email: `client@ecommerce.com`
- First Name: `Client`
- Last Name: `User`
- Enabled: `Yes`
- Email Verified: `Yes`
- Credentials: password = `client123` (Temporary: OFF)
- Role Mappings: `CLIENT`

### 4. Créer le Client Frontend

1. Allez dans "Clients" > "Create client"
2. Client ID: `ecommerce-frontend`
3. Client Protocol: `openid-connect`
4. Cliquez sur "Next"
5. Client authentication: `OFF` (Public client)
6. Authorization: `OFF`
7. Standard flow: `ON`
8. Direct access grants: `ON`
9. Cliquez sur "Next" puis "Save"

#### Configuration du Client
- Root URL: `http://localhost:3000`
- Valid redirect URIs: `http://localhost:3000/*`
- Web origins: `http://localhost:3000` et `+`
- Advanced Settings > Proof Key for Code Exchange: `S256`

### 5. Vérifier les Scopes

Dans "Client scopes", assurez-vous que les mappers suivants sont configurés :
- `realm roles` mapper dans le scope `roles`

## Test de la configuration

### Obtenir un token (pour tests)

```bash
# Token pour admin
curl -X POST "http://localhost:8180/realms/ecommerce/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=ecommerce-frontend" \
  -d "username=admin" \
  -d "password=admin123"

# Token pour client
curl -X POST "http://localhost:8180/realms/ecommerce/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=ecommerce-frontend" \
  -d "username=client" \
  -d "password=client123"
```

### Décoder le token

Utilisez https://jwt.io pour décoder le token et vérifier que :
- `realm_access.roles` contient le rôle approprié
- `preferred_username` contient le nom d'utilisateur

## URLs Keycloak

- Console Admin: http://localhost:8180/admin
- Endpoint Token: http://localhost:8180/realms/ecommerce/protocol/openid-connect/token
- Endpoint UserInfo: http://localhost:8180/realms/ecommerce/protocol/openid-connect/userinfo
- JWKS: http://localhost:8180/realms/ecommerce/protocol/openid-connect/certs
- OpenID Configuration: http://localhost:8180/realms/ecommerce/.well-known/openid-configuration
