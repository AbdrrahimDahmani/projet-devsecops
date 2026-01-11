import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useKeycloak } from '@react-keycloak/web';
import {
  Box,
  Button,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
  Chip,
  CircularProgress,
} from '@mui/material';
import { Add as AddIcon, Visibility as ViewIcon } from '@mui/icons-material';
import { toast } from 'react-toastify';
import { orderApi } from '../services/api';

const getStatusColor = (status) => {
  const colors = {
    EN_ATTENTE: 'warning',
    CONFIRMEE: 'info',
    EN_PREPARATION: 'info',
    EXPEDIEE: 'primary',
    LIVREE: 'success',
    ANNULEE: 'error',
  };
  return colors[status] || 'default';
};

function OrderList() {
  const { keycloak } = useKeycloak();
  const navigate = useNavigate();
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);

  const isAdmin = keycloak.hasRealmRole('ADMIN');
  const isClient = keycloak.hasRealmRole('CLIENT');

  useEffect(() => {
    fetchOrders();
  }, [isAdmin]);

  const fetchOrders = async () => {
    try {
      setLoading(true);
      let response;
      if (isAdmin) {
        response = await orderApi.getAll();
      } else {
        response = await orderApi.getMyOrders();
      }
      setOrders(response.data);
    } catch (error) {
      console.error('Erreur lors du chargement des commandes', error);
      toast.error('Erreur lors du chargement des commandes');
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString('fr-FR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4">
          {isAdmin ? 'Toutes les Commandes' : 'Mes Commandes'}
        </Typography>
        {isClient && (
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => navigate('/commandes/nouvelle')}
          >
            Nouvelle Commande
          </Button>
        )}
      </Box>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>N° Commande</TableCell>
              {isAdmin && <TableCell>Client</TableCell>}
              <TableCell>Date</TableCell>
              <TableCell>Statut</TableCell>
              <TableCell align="right">Montant Total</TableCell>
              <TableCell align="center">Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {orders.map((order) => (
              <TableRow key={order.id} hover>
                <TableCell>#{order.id}</TableCell>
                {isAdmin && <TableCell>{order.username}</TableCell>}
                <TableCell>{formatDate(order.dateCommande)}</TableCell>
                <TableCell>
                  <Chip
                    label={order.statutLabel}
                    color={getStatusColor(order.statut)}
                    size="small"
                  />
                </TableCell>
                <TableCell align="right">
                  {parseFloat(order.montantTotal).toFixed(2)} €
                </TableCell>
                <TableCell align="center">
                  <Button
                    size="small"
                    startIcon={<ViewIcon />}
                    onClick={() => navigate(`/commandes/${order.id}`)}
                  >
                    Détails
                  </Button>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      {orders.length === 0 && (
        <Box textAlign="center" py={5}>
          <Typography variant="h6" color="textSecondary">
            Aucune commande trouvée
          </Typography>
          {isClient && (
            <Button
              variant="contained"
              sx={{ mt: 2 }}
              onClick={() => navigate('/commandes/nouvelle')}
            >
              Créer ma première commande
            </Button>
          )}
        </Box>
      )}
    </Box>
  );
}

export default OrderList;
