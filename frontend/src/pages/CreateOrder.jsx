import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Box,
  Button,
  Card,
  CardContent,
  Divider,
  Grid,
  IconButton,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Typography,
  CircularProgress,
} from '@mui/material';
import {
  Add as AddIcon,
  Remove as RemoveIcon,
  Delete as DeleteIcon,
  ShoppingCart as CartIcon,
  ArrowBack as BackIcon,
} from '@mui/icons-material';
import { toast } from 'react-toastify';
import { productApi, orderApi } from '../services/api';

function CreateOrder() {
  const navigate = useNavigate();
  const [products, setProducts] = useState([]);
  const [cart, setCart] = useState([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    fetchProducts();
  }, []);

  const fetchProducts = async () => {
    try {
      setLoading(true);
      const response = await productApi.getAll();
      setProducts(response.data.filter(p => p.quantiteStock > 0));
    } catch (error) {
      console.error('Erreur lors du chargement des produits', error);
      toast.error('Erreur lors du chargement des produits');
    } finally {
      setLoading(false);
    }
  };

  const addToCart = (product) => {
    setCart((prevCart) => {
      const existingItem = prevCart.find((item) => item.productId === product.id);
      if (existingItem) {
        if (existingItem.quantite >= product.quantiteStock) {
          toast.warning('Stock insuffisant');
          return prevCart;
        }
        return prevCart.map((item) =>
          item.productId === product.id
            ? { ...item, quantite: item.quantite + 1 }
            : item
        );
      }
      return [
        ...prevCart,
        {
          productId: product.id,
          nom: product.nom,
          prix: product.prix,
          quantite: 1,
          maxStock: product.quantiteStock,
        },
      ];
    });
  };

  const updateQuantity = (productId, delta) => {
    setCart((prevCart) =>
      prevCart
        .map((item) => {
          if (item.productId === productId) {
            const newQuantity = item.quantite + delta;
            if (newQuantity > item.maxStock) {
              toast.warning('Stock insuffisant');
              return item;
            }
            return { ...item, quantite: Math.max(0, newQuantity) };
          }
          return item;
        })
        .filter((item) => item.quantite > 0)
    );
  };

  const removeFromCart = (productId) => {
    setCart((prevCart) => prevCart.filter((item) => item.productId !== productId));
  };

  const calculateTotal = () => {
    return cart.reduce((sum, item) => sum + item.prix * item.quantite, 0);
  };

  const handleSubmit = async () => {
    if (cart.length === 0) {
      toast.error('Votre panier est vide');
      return;
    }

    try {
      setSubmitting(true);
      const orderData = {
        items: cart.map((item) => ({
          productId: item.productId,
          quantite: item.quantite,
        })),
      };

      const response = await orderApi.create(orderData);
      toast.success('Commande créée avec succès !');
      navigate(`/commandes/${response.data.id}`);
    } catch (error) {
      console.error('Erreur lors de la création de la commande', error);
      const message = error.response?.data?.message || 'Erreur lors de la création de la commande';
      toast.error(message);
    } finally {
      setSubmitting(false);
    }
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
      <Button
        startIcon={<BackIcon />}
        onClick={() => navigate('/commandes')}
        sx={{ mb: 2 }}
      >
        Retour aux commandes
      </Button>

      <Typography variant="h4" gutterBottom>
        Nouvelle Commande
      </Typography>

      <Grid container spacing={3}>
        {/* Liste des produits */}
        <Grid item xs={12} md={8}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Produits disponibles
              </Typography>
              <TableContainer component={Paper} variant="outlined">
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      <TableCell>Produit</TableCell>
                      <TableCell align="right">Prix</TableCell>
                      <TableCell align="center">Stock</TableCell>
                      <TableCell align="center">Action</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {products.map((product) => (
                      <TableRow key={product.id} hover>
                        <TableCell>
                          <Typography variant="body2" fontWeight="medium">
                            {product.nom}
                          </Typography>
                          <Typography variant="caption" color="textSecondary">
                            {product.description?.substring(0, 50)}...
                          </Typography>
                        </TableCell>
                        <TableCell align="right">
                          {parseFloat(product.prix).toFixed(2)} €
                        </TableCell>
                        <TableCell align="center">
                          {product.quantiteStock}
                        </TableCell>
                        <TableCell align="center">
                          <Button
                            size="small"
                            variant="outlined"
                            startIcon={<AddIcon />}
                            onClick={() => addToCart(product)}
                          >
                            Ajouter
                          </Button>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            </CardContent>
          </Card>
        </Grid>

        {/* Panier */}
        <Grid item xs={12} md={4}>
          <Card>
            <CardContent>
              <Box display="flex" alignItems="center" mb={2}>
                <CartIcon sx={{ mr: 1 }} />
                <Typography variant="h6">
                  Panier ({cart.length} article{cart.length > 1 ? 's' : ''})
                </Typography>
              </Box>

              {cart.length === 0 ? (
                <Typography color="textSecondary" textAlign="center" py={3}>
                  Votre panier est vide
                </Typography>
              ) : (
                <>
                  {cart.map((item) => (
                    <Box key={item.productId} mb={2}>
                      <Box display="flex" justifyContent="space-between" alignItems="center">
                        <Typography variant="body2" fontWeight="medium">
                          {item.nom}
                        </Typography>
                        <IconButton
                          size="small"
                          color="error"
                          onClick={() => removeFromCart(item.productId)}
                        >
                          <DeleteIcon fontSize="small" />
                        </IconButton>
                      </Box>
                      <Box display="flex" justifyContent="space-between" alignItems="center" mt={1}>
                        <Box display="flex" alignItems="center">
                          <IconButton
                            size="small"
                            onClick={() => updateQuantity(item.productId, -1)}
                          >
                            <RemoveIcon fontSize="small" />
                          </IconButton>
                          <TextField
                            size="small"
                            value={item.quantite}
                            inputProps={{ 
                              style: { textAlign: 'center', width: 40 },
                              readOnly: true 
                            }}
                          />
                          <IconButton
                            size="small"
                            onClick={() => updateQuantity(item.productId, 1)}
                          >
                            <AddIcon fontSize="small" />
                          </IconButton>
                        </Box>
                        <Typography>
                          {(item.prix * item.quantite).toFixed(2)} €
                        </Typography>
                      </Box>
                      <Divider sx={{ mt: 1 }} />
                    </Box>
                  ))}

                  <Box display="flex" justifyContent="space-between" mt={2} mb={2}>
                    <Typography variant="h6">Total</Typography>
                    <Typography variant="h6" color="primary">
                      {calculateTotal().toFixed(2)} €
                    </Typography>
                  </Box>

                  <Button
                    fullWidth
                    variant="contained"
                    size="large"
                    onClick={handleSubmit}
                    disabled={submitting}
                    startIcon={submitting ? <CircularProgress size={20} /> : <CartIcon />}
                  >
                    {submitting ? 'Création en cours...' : 'Valider la commande'}
                  </Button>
                </>
              )}
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
}

export default CreateOrder;
