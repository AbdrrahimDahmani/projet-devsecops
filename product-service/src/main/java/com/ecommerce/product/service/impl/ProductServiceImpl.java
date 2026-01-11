package com.ecommerce.product.service.impl;

import com.ecommerce.product.dto.ProductDTO;
import com.ecommerce.product.dto.StockCheckRequest;
import com.ecommerce.product.dto.StockCheckResponse;
import com.ecommerce.product.dto.StockUpdateRequest;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.exception.ProductNotFoundException;
import com.ecommerce.product.exception.InsufficientStockException;
import com.ecommerce.product.repository.ProductRepository;
import com.ecommerce.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts() {
        log.info("Récupération de tous les produits actifs");
        return productRepository.findByActifTrue()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id) {
        log.info("Récupération du produit avec l'ID: {}", id);
        Product product = productRepository.findByIdAndActifTrue(id)
                .orElseThrow(() -> new ProductNotFoundException("Produit non trouvé avec l'ID: " + id));
        return mapToDTO(product);
    }

    @Override
    public ProductDTO createProduct(ProductDTO productDTO) {
        log.info("Création d'un nouveau produit: {}", productDTO.getNom());
        
        Product product = Product.builder()
                .nom(productDTO.getNom())
                .description(productDTO.getDescription())
                .prix(productDTO.getPrix())
                .quantiteStock(productDTO.getQuantiteStock())
                .actif(true)
                .build();

        Product savedProduct = productRepository.save(product);
        log.info("Produit créé avec succès avec l'ID: {}", savedProduct.getId());
        return mapToDTO(savedProduct);
    }

    @Override
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        log.info("Mise à jour du produit avec l'ID: {}", id);
        
        Product existingProduct = productRepository.findByIdAndActifTrue(id)
                .orElseThrow(() -> new ProductNotFoundException("Produit non trouvé avec l'ID: " + id));

        existingProduct.setNom(productDTO.getNom());
        existingProduct.setDescription(productDTO.getDescription());
        existingProduct.setPrix(productDTO.getPrix());
        existingProduct.setQuantiteStock(productDTO.getQuantiteStock());

        Product updatedProduct = productRepository.save(existingProduct);
        log.info("Produit mis à jour avec succès: {}", updatedProduct.getId());
        return mapToDTO(updatedProduct);
    }

    @Override
    public void deleteProduct(Long id) {
        log.info("Suppression (soft delete) du produit avec l'ID: {}", id);
        
        Product product = productRepository.findByIdAndActifTrue(id)
                .orElseThrow(() -> new ProductNotFoundException("Produit non trouvé avec l'ID: " + id));

        product.setActif(false);
        productRepository.save(product);
        log.info("Produit désactivé avec succès: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> searchProducts(String keyword) {
        log.info("Recherche de produits avec le mot-clé: {}", keyword);
        return productRepository.searchByNom(keyword)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public StockCheckResponse checkStock(StockCheckRequest request) {
        log.info("Vérification du stock pour le produit ID: {}, quantité demandée: {}", 
                request.getProductId(), request.getQuantite());

        Product product = productRepository.findByIdAndActifTrue(request.getProductId())
                .orElse(null);

        if (product == null) {
            return StockCheckResponse.builder()
                    .productId(request.getProductId())
                    .quantiteDemandee(request.getQuantite())
                    .disponible(false)
                    .message("Produit non trouvé")
                    .build();
        }

        boolean disponible = product.getQuantiteStock() >= request.getQuantite();
        
        return StockCheckResponse.builder()
                .productId(product.getId())
                .nomProduit(product.getNom())
                .prixUnitaire(product.getPrix())
                .quantiteDemandee(request.getQuantite())
                .quantiteDisponible(product.getQuantiteStock())
                .disponible(disponible)
                .message(disponible ? "Stock suffisant" : "Stock insuffisant")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockCheckResponse> checkMultipleStock(List<StockCheckRequest> requests) {
        log.info("Vérification du stock pour {} produits", requests.size());
        return requests.stream()
                .map(this::checkStock)
                .collect(Collectors.toList());
    }

    @Override
    public boolean decrementStock(StockUpdateRequest request) {
        log.info("Décrémentation du stock pour le produit ID: {}, quantité: {}", 
                request.getProductId(), request.getQuantite());

        Product product = productRepository.findByIdAndActifTrue(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Produit non trouvé avec l'ID: " + request.getProductId()));

        if (product.getQuantiteStock() < request.getQuantite()) {
            throw new InsufficientStockException(
                    "Stock insuffisant pour le produit " + product.getNom() + 
                    ". Disponible: " + product.getQuantiteStock() + 
                    ", Demandé: " + request.getQuantite());
        }

        int updated = productRepository.decrementStock(request.getProductId(), request.getQuantite());
        log.info("Stock décrémenté: {} lignes mises à jour", updated);
        return updated > 0;
    }

    @Override
    public boolean incrementStock(StockUpdateRequest request) {
        log.info("Incrémentation du stock pour le produit ID: {}, quantité: {}", 
                request.getProductId(), request.getQuantite());

        if (!productRepository.existsById(request.getProductId())) {
            throw new ProductNotFoundException("Produit non trouvé avec l'ID: " + request.getProductId());
        }

        int updated = productRepository.incrementStock(request.getProductId(), request.getQuantite());
        log.info("Stock incrémenté: {} lignes mises à jour", updated);
        return updated > 0;
    }

    private ProductDTO mapToDTO(Product product) {
        return ProductDTO.builder()
                .id(product.getId())
                .nom(product.getNom())
                .description(product.getDescription())
                .prix(product.getPrix())
                .quantiteStock(product.getQuantiteStock())
                .actif(product.getActif())
                .build();
    }
}
