import React, { useState, useEffect } from 'react';
import { useKeycloak } from '@react-keycloak/web';
import {
  Grid,
  Card,
  CardContent,
  Typography,
  Box,
  CircularProgress,
} from '@mui/material';
import {
  Inventory as InventoryIcon,
  ShoppingCart as ShoppingCartIcon,
  AttachMoney as MoneyIcon,
  TrendingUp as TrendingIcon,
} from '@mui/icons-material';
import { productApi, orderApi } from '../services/api';

function Dashboard() {
  const { keycloak } = useKeycloak();
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState({
    products: 0,
    orders: 0,
    totalRevenue: 0,
  });

  const isAdmin = keycloak.hasRealmRole('ADMIN');
  const username = keycloak.tokenParsed?.preferred_username || 'Utilisateur';

  useEffect(() => {
    const fetchStats = async () => {
      try {
        const productsRes = await productApi.getAll();
        let ordersRes;
        
        if (isAdmin) {
          ordersRes = await orderApi.getAll();
        } else {
          ordersRes = await orderApi.getMyOrders();
        }

        const totalRevenue = ordersRes.data.reduce(
          (sum, order) => sum + parseFloat(order.montantTotal || 0), 
          0
        );

        setStats({
          products: productsRes.data.length,
          orders: ordersRes.data.length,
          totalRevenue: totalRevenue,
        });
      } catch (error) {
        console.error('Erreur lors du chargement des statistiques', error);
      } finally {
        setLoading(false);
      }
    };

    fetchStats();
  }, [isAdmin]);

  const StatCard = ({ title, value, icon, color }) => (
    <Card sx={{ height: '100%' }}>
      <CardContent>
        <Box display="flex" alignItems="center" justifyContent="space-between">
          <Box>
            <Typography color="textSecondary" gutterBottom variant="overline">
              {title}
            </Typography>
            <Typography variant="h4" component="div">
              {loading ? <CircularProgress size={24} /> : value}
            </Typography>
          </Box>
          <Box
            sx={{
              backgroundColor: color,
              borderRadius: '50%',
              p: 1.5,
              display: 'flex',
            }}
          >
            {icon}
          </Box>
        </Box>
      </CardContent>
    </Card>
  );

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Bienvenue, {username} !
      </Typography>
      <Typography variant="body1" color="textSecondary" paragraph>
        {isAdmin 
          ? 'Vous êtes connecté en tant qu\'administrateur.'
          : 'Vous êtes connecté en tant que client.'}
      </Typography>

      <Grid container spacing={3} sx={{ mt: 2 }}>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Produits"
            value={stats.products}
            icon={<InventoryIcon sx={{ color: 'white' }} />}
            color="#1976d2"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title={isAdmin ? "Total Commandes" : "Mes Commandes"}
            value={stats.orders}
            icon={<ShoppingCartIcon sx={{ color: 'white' }} />}
            color="#388e3c"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Chiffre d'affaires"
            value={`${stats.totalRevenue.toFixed(2)} €`}
            icon={<MoneyIcon sx={{ color: 'white' }} />}
            color="#f57c00"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Croissance"
            value="+12%"
            icon={<TrendingIcon sx={{ color: 'white' }} />}
            color="#7b1fa2"
          />
        </Grid>
      </Grid>

      <Box sx={{ mt: 4 }}>
        <Typography variant="h5" gutterBottom>
          Actions rapides
        </Typography>
        <Typography variant="body2" color="textSecondary">
          Utilisez le menu latéral pour accéder aux différentes fonctionnalités de l'application.
        </Typography>
      </Box>
    </Box>
  );
}

export default Dashboard;
