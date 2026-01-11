import Keycloak from 'keycloak-js';

const keycloakConfig = {
  url: import.meta.env.VITE_KEYCLOAK_URL || 'http://localhost:8180',
  realm: import.meta.env.VITE_KEYCLOAK_REALM || 'ecommerce',
  clientId: import.meta.env.VITE_KEYCLOAK_CLIENT_ID || 'ecommerce-frontend',
};

const keycloak = new Keycloak(keycloakConfig);

export default keycloak;
