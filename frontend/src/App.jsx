import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { useKeycloak } from '@react-keycloak/web';
import { ThemeProvider, createTheme, CssBaseline } from '@mui/material';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

// Components
import Layout from './components/Layout';
import Dashboard from './pages/Dashboard';
import ProductList from './pages/ProductList';
import ProductForm from './pages/ProductForm';
import OrderList from './pages/OrderList';
import OrderDetail from './pages/OrderDetail';
import CreateOrder from './pages/CreateOrder';
import Unauthorized from './pages/Unauthorized';

const theme = createTheme({
  palette: {
    primary: {
      main: '#1976d2',
    },
    secondary: {
      main: '#dc004e',
    },
  },
  typography: {
    fontFamily: 'Roboto, sans-serif',
  },
});

// Composant pour les routes protégées par rôle
const ProtectedRoute = ({ children, roles }) => {
  const { keycloak } = useKeycloak();

  const hasRequiredRole = roles.some(role => 
    keycloak.hasRealmRole(role)
  );

  if (!hasRequiredRole) {
    return <Navigate to="/unauthorized" replace />;
  }

  return children;
};

function App() {
  const { keycloak, initialized } = useKeycloak();

  if (!initialized) {
    return null;
  }

  if (!keycloak.authenticated) {
    return null;
  }

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Router>
        <Layout>
          <Routes>
            <Route path="/" element={<Dashboard />} />
            
            {/* Routes Produits */}
            <Route path="/produits" element={<ProductList />} />
            <Route 
              path="/produits/nouveau" 
              element={
                <ProtectedRoute roles={['ADMIN']}>
                  <ProductForm />
                </ProtectedRoute>
              } 
            />
            <Route 
              path="/produits/modifier/:id" 
              element={
                <ProtectedRoute roles={['ADMIN']}>
                  <ProductForm />
                </ProtectedRoute>
              } 
            />

            {/* Routes Commandes */}
            <Route path="/commandes" element={<OrderList />} />
            <Route path="/commandes/:id" element={<OrderDetail />} />
            <Route 
              path="/commandes/nouvelle" 
              element={
                <ProtectedRoute roles={['CLIENT']}>
                  <CreateOrder />
                </ProtectedRoute>
              } 
            />

            {/* Route non autorisée */}
            <Route path="/unauthorized" element={<Unauthorized />} />

            {/* Redirection par défaut */}
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </Layout>
      </Router>
      <ToastContainer 
        position="bottom-right"
        autoClose={3000}
        hideProgressBar={false}
        newestOnTop
        closeOnClick
        rtl={false}
        pauseOnFocusLoss
        draggable
        pauseOnHover
      />
    </ThemeProvider>
  );
}

export default App;
