import axios from 'axios';
import keycloak from '../keycloak';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Intercepteur pour ajouter le token JWT
api.interceptors.request.use(
  async (config) => {
    if (keycloak.authenticated) {
      // Rafraîchir le token si nécessaire
      try {
        await keycloak.updateToken(30);
        config.headers.Authorization = `Bearer ${keycloak.token}`;
      } catch (error) {
        console.error('Échec du rafraîchissement du token', error);
        keycloak.logout();
      }
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Intercepteur pour gérer les erreurs
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response) {
      const { status } = error.response;
      
      if (status === 401) {
        console.error('Non autorisé - Redirection vers login');
        keycloak.logout();
      } else if (status === 403) {
        console.error('Accès interdit');
      }
    }
    return Promise.reject(error);
  }
);

// API Produits
export const productApi = {
  getAll: () => api.get('/api/produits'),
  getById: (id) => api.get(`/api/produits/${id}`),
  create: (product) => api.post('/api/produits', product),
  update: (id, product) => api.put(`/api/produits/${id}`, product),
  delete: (id) => api.delete(`/api/produits/${id}`),
  search: (keyword) => api.get(`/api/produits/search?keyword=${keyword}`),
};

// API Commandes
export const orderApi = {
  getMyOrders: () => api.get('/api/commandes/mes-commandes'),
  getAll: () => api.get('/api/commandes'),
  getById: (id) => api.get(`/api/commandes/${id}`),
  create: (order) => api.post('/api/commandes', order),
  cancel: (id) => api.post(`/api/commandes/${id}/annuler`),
};

export default api;
