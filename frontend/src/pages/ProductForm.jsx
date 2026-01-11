import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  Box,
  Button,
  Card,
  CardContent,
  TextField,
  Typography,
  Grid,
  CircularProgress,
} from '@mui/material';
import { Save as SaveIcon, ArrowBack as BackIcon } from '@mui/icons-material';
import { toast } from 'react-toastify';
import { productApi } from '../services/api';

function ProductForm() {
  const navigate = useNavigate();
  const { id } = useParams();
  const isEditing = Boolean(id);

  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [formData, setFormData] = useState({
    nom: '',
    description: '',
    prix: '',
    quantiteStock: '',
  });
  const [errors, setErrors] = useState({});

  useEffect(() => {
    if (isEditing) {
      fetchProduct();
    }
  }, [id]);

  const fetchProduct = async () => {
    try {
      setLoading(true);
      const response = await productApi.getById(id);
      setFormData({
        nom: response.data.nom || '',
        description: response.data.description || '',
        prix: response.data.prix?.toString() || '',
        quantiteStock: response.data.quantiteStock?.toString() || '',
      });
    } catch (error) {
      console.error('Erreur lors du chargement du produit', error);
      toast.error('Erreur lors du chargement du produit');
      navigate('/produits');
    } finally {
      setLoading(false);
    }
  };

  const validateForm = () => {
    const newErrors = {};

    if (!formData.nom.trim()) {
      newErrors.nom = 'Le nom est obligatoire';
    } else if (formData.nom.length < 2) {
      newErrors.nom = 'Le nom doit contenir au moins 2 caractères';
    }

    if (!formData.prix) {
      newErrors.prix = 'Le prix est obligatoire';
    } else if (parseFloat(formData.prix) <= 0) {
      newErrors.prix = 'Le prix doit être supérieur à 0';
    }

    if (!formData.quantiteStock) {
      newErrors.quantiteStock = 'La quantité en stock est obligatoire';
    } else if (parseInt(formData.quantiteStock) < 0) {
      newErrors.quantiteStock = 'La quantité ne peut pas être négative';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    if (errors[name]) {
      setErrors((prev) => ({ ...prev, [name]: '' }));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!validateForm()) {
      return;
    }

    try {
      setSaving(true);
      const productData = {
        nom: formData.nom,
        description: formData.description,
        prix: parseFloat(formData.prix),
        quantiteStock: parseInt(formData.quantiteStock),
      };

      if (isEditing) {
        await productApi.update(id, productData);
        toast.success('Produit modifié avec succès');
      } else {
        await productApi.create(productData);
        toast.success('Produit créé avec succès');
      }

      navigate('/produits');
    } catch (error) {
      console.error('Erreur lors de la sauvegarde', error);
      if (error.response?.data?.validationErrors) {
        setErrors(error.response.data.validationErrors);
      } else {
        toast.error('Erreur lors de la sauvegarde du produit');
      }
    } finally {
      setSaving(false);
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
        onClick={() => navigate('/produits')}
        sx={{ mb: 2 }}
      >
        Retour à la liste
      </Button>

      <Typography variant="h4" gutterBottom>
        {isEditing ? 'Modifier le produit' : 'Nouveau produit'}
      </Typography>

      <Card>
        <CardContent>
          <form onSubmit={handleSubmit}>
            <Grid container spacing={3}>
              <Grid item xs={12}>
                <TextField
                  fullWidth
                  label="Nom du produit"
                  name="nom"
                  value={formData.nom}
                  onChange={handleChange}
                  error={Boolean(errors.nom)}
                  helperText={errors.nom}
                  required
                />
              </Grid>

              <Grid item xs={12}>
                <TextField
                  fullWidth
                  label="Description"
                  name="description"
                  value={formData.description}
                  onChange={handleChange}
                  multiline
                  rows={4}
                  error={Boolean(errors.description)}
                  helperText={errors.description}
                />
              </Grid>

              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="Prix (€)"
                  name="prix"
                  type="number"
                  value={formData.prix}
                  onChange={handleChange}
                  error={Boolean(errors.prix)}
                  helperText={errors.prix}
                  inputProps={{ step: '0.01', min: '0.01' }}
                  required
                />
              </Grid>

              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="Quantité en stock"
                  name="quantiteStock"
                  type="number"
                  value={formData.quantiteStock}
                  onChange={handleChange}
                  error={Boolean(errors.quantiteStock)}
                  helperText={errors.quantiteStock}
                  inputProps={{ min: '0' }}
                  required
                />
              </Grid>

              <Grid item xs={12}>
                <Box display="flex" gap={2} justifyContent="flex-end">
                  <Button
                    variant="outlined"
                    onClick={() => navigate('/produits')}
                  >
                    Annuler
                  </Button>
                  <Button
                    type="submit"
                    variant="contained"
                    startIcon={saving ? <CircularProgress size={20} /> : <SaveIcon />}
                    disabled={saving}
                  >
                    {isEditing ? 'Modifier' : 'Créer'}
                  </Button>
                </Box>
              </Grid>
            </Grid>
          </form>
        </CardContent>
      </Card>
    </Box>
  );
}

export default ProductForm;
