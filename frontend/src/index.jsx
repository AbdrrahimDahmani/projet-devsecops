import React from 'react';
import ReactDOM from 'react-dom/client';
import { ReactKeycloakProvider } from '@react-keycloak/web';
import App from './App';
import keycloak from './keycloak';
import { CircularProgress, Box, Typography } from '@mui/material';

const LoadingComponent = () => (
  <Box
    display="flex"
    flexDirection="column"
    justifyContent="center"
    alignItems="center"
    minHeight="100vh"
    bgcolor="#f5f5f5"
  >
    <CircularProgress size={60} />
    <Typography variant="h6" sx={{ mt: 2 }}>
      Initialisation de l'authentification...
    </Typography>
  </Box>
);

const initOptions = {
  onLoad: 'login-required',
  checkLoginIframe: false,
  pkceMethod: 'S256',
};

const handleOnEvent = (event, error) => {
  if (event === 'onAuthSuccess') {
    console.log('Authentification réussie');
  }
  if (event === 'onAuthError') {
    console.error('Erreur d\'authentification', error);
  }
  if (event === 'onTokenExpired') {
    console.log('Token expiré, rafraîchissement...');
    keycloak.updateToken(30).catch(() => {
      console.error('Échec du rafraîchissement, déconnexion...');
      keycloak.logout();
    });
  }
};

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
  <React.StrictMode>
    <ReactKeycloakProvider
      authClient={keycloak}
      initOptions={initOptions}
      LoadingComponent={<LoadingComponent />}
      onEvent={handleOnEvent}
    >
      <App />
    </ReactKeycloakProvider>
  </React.StrictMode>
);
