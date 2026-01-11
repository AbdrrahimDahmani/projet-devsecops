package com.ecommerce.product.service;

import com.ecommerce.product.dto.ProductDTO;
import com.ecommerce.product.dto.StockCheckRequest;
import com.ecommerce.product.dto.StockCheckResponse;
import com.ecommerce.product.dto.StockUpdateRequest;

import java.util.List;

public interface ProductService {

    List<ProductDTO> getAllProducts();

    ProductDTO getProductById(Long id);

    ProductDTO createProduct(ProductDTO productDTO);

    ProductDTO updateProduct(Long id, ProductDTO productDTO);

    void deleteProduct(Long id);

    List<ProductDTO> searchProducts(String keyword);

    StockCheckResponse checkStock(StockCheckRequest request);

    List<StockCheckResponse> checkMultipleStock(List<StockCheckRequest> requests);

    boolean decrementStock(StockUpdateRequest request);

    boolean incrementStock(StockUpdateRequest request);
}
