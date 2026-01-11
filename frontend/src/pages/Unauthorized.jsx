import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Box, Button, Typography } from '@mui/material';
import { Lock as LockIcon, Home as HomeIcon } from '@mui/icons-material';

function Unauthorized() {
  const navigate = useNavigate();

  return (
    <Box
      display="flex"
      flexDirection="column"
      justifyContent="center"
      alignItems="center"
      minHeight="60vh"
      textAlign="center"
    >
      <LockIcon sx={{ fontSize: 80, color: 'error.main', mb: 2 }} />
      <Typography variant="h4" gutterBottom>
        Accès Refusé
      </Typography>
      <Typography variant="body1" color="textSecondary" paragraph>
        Vous n'avez pas les permissions nécessaires pour accéder à cette page.
      </Typography>
      <Button
        variant="contained"
        startIcon={<HomeIcon />}
        onClick={() => navigate('/')}
      >
        Retour à l'accueil
      </Button>
    </Box>
  );
}

export default Unauthorized;
