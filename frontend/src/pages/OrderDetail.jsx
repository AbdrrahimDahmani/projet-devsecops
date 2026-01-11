import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useKeycloak } from '@react-keycloak/web';
import {
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  Divider,
  Grid,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
  CircularProgress,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
} from '@mui/material';
import { ArrowBack as BackIcon, Cancel as CancelIcon } from '@mui/icons-material';
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

function OrderDetail() {
  const { keycloak } = useKeycloak();
  const navigate = useNavigate();
  const { id } = useParams();
  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(true);
  const [cancelDialogOpen, setCancelDialogOpen] = useState(false);
  const [cancelling, setCancelling] = useState(false);

  const isAdmin = keycloak.hasRealmRole('ADMIN');

  useEffect(() => {
    fetchOrder();
  }, [id]);

  const fetchOrder = async () => {
    try {
      setLoading(true);
      const response = await orderApi.getById(id);
      setOrder(response.data);
    } catch (error) {
      console.error('Erreur lors du chargement de la commande', error);
      if (error.response?.status === 403) {
        toast.error('Vous n\'avez pas accès à cette commande');
      } else {
        toast.error('Erreur lors du chargement de la commande');
      }
      navigate('/commandes');
    } finally {
      setLoading(false);
    }
  };

  const handleCancelOrder = async () => {
    try {
      setCancelling(true);
      await orderApi.cancel(id);
      toast.success('Commande annulée avec succès');
      fetchOrder();
    } catch (error) {
      console.error('Erreur lors de l\'annulation', error);
      toast.error(error.response?.data?.message || 'Erreur lors de l\'annulation');
    } finally {
      setCancelling(false);
      setCancelDialogOpen(false);
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

  const canCancel = order && !['ANNULEE', 'LIVREE', 'EXPEDIEE'].includes(order.statut);

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
        <CircularProgress />
      </Box>
    );
  }

  if (!order) {
    return null;
  }

  return (
    <Box>
      <Button
        startIcon={<BackIcon />}
        onClick={() => navigate('/commandes')}
        sx={{ mb: 2 }}
      >
        Retour à la liste
      </Button>

      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4">
          Commande #{order.id}
        </Typography>
        {canCancel && (
          <Button
            variant="outlined"
            color="error"
            startIcon={<CancelIcon />}
            onClick={() => setCancelDialogOpen(true)}
          >
            Annuler la commande
          </Button>
        )}
      </Box>

      <Grid container spacing={3}>
        <Grid item xs={12} md={8}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Détails de la commande
              </Typography>
              <Divider sx={{ mb: 2 }} />
              
              <Grid container spacing={2}>
                <Grid item xs={6}>
                  <Typography variant="body2" color="textSecondary">
                    Date de commande
                  </Typography>
                  <Typography variant="body1">
                    {formatDate(order.dateCommande)}
                  </Typography>
                </Grid>
                <Grid item xs={6}>
                  <Typography variant="body2" color="textSecondary">
                    Statut
                  </Typography>
                  <Chip
                    label={order.statutLabel}
                    color={getStatusColor(order.statut)}
                    size="small"
                  />
                </Grid>
                {isAdmin && (
                  <Grid item xs={6}>
                    <Typography variant="body2" color="textSecondary">
                      Client
                    </Typography>
                    <Typography variant="body1">
                      {order.username}
                    </Typography>
                  </Grid>
                )}
              </Grid>
            </CardContent>
          </Card>

          <Card sx={{ mt: 3 }}>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Produits commandés
              </Typography>
              <TableContainer component={Paper} variant="outlined">
                <Table>
                  <TableHead>
                    <TableRow>
                      <TableCell>Produit</TableCell>
                      <TableCell align="center">Quantité</TableCell>
                      <TableCell align="right">Prix unitaire</TableCell>
                      <TableCell align="right">Sous-total</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {order.items.map((item) => (
                      <TableRow key={item.id}>
                        <TableCell>{item.nomProduit}</TableCell>
                        <TableCell align="center">{item.quantite}</TableCell>
                        <TableCell align="right">
                          {parseFloat(item.prixUnitaire).toFixed(2)} €
                        </TableCell>
                        <TableCell align="right">
                          {parseFloat(item.sousTotal).toFixed(2)} €
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={4}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Récapitulatif
              </Typography>
              <Divider sx={{ mb: 2 }} />
              
              <Box display="flex" justifyContent="space-between" mb={1}>
                <Typography>Nombre d'articles</Typography>
                <Typography>
                  {order.items.reduce((sum, item) => sum + item.quantite, 0)}
                </Typography>
              </Box>
              
              <Box display="flex" justifyContent="space-between" mb={1}>
                <Typography>Sous-total</Typography>
                <Typography>
                  {parseFloat(order.montantTotal).toFixed(2)} €
                </Typography>
              </Box>
              
              <Box display="flex" justifyContent="space-between" mb={1}>
                <Typography>Livraison</Typography>
                <Typography color="success.main">Gratuite</Typography>
              </Box>
              
              <Divider sx={{ my: 2 }} />
              
              <Box display="flex" justifyContent="space-between">
                <Typography variant="h6">Total</Typography>
                <Typography variant="h6" color="primary">
                  {parseFloat(order.montantTotal).toFixed(2)} €
                </Typography>
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Dialog de confirmation d'annulation */}
      <Dialog open={cancelDialogOpen} onClose={() => setCancelDialogOpen(false)}>
        <DialogTitle>Confirmer l'annulation</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Êtes-vous sûr de vouloir annuler cette commande ?
            Les produits seront remis en stock.
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setCancelDialogOpen(false)}>
            Non, garder la commande
          </Button>
          <Button
            onClick={handleCancelOrder}
            color="error"
            variant="contained"
            disabled={cancelling}
          >
            {cancelling ? <CircularProgress size={20} /> : 'Oui, annuler'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}

export default OrderDetail;
