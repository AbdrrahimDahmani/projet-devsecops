package com.ecommerce.product.repository;

import com.ecommerce.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByActifTrue();

    Optional<Product> findByIdAndActifTrue(Long id);

    @Query("SELECT p FROM Product p WHERE p.actif = true AND LOWER(p.nom) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> searchByNom(@Param("keyword") String keyword);

    @Query("SELECT p FROM Product p WHERE p.quantiteStock < :threshold AND p.actif = true")
    List<Product> findLowStockProducts(@Param("threshold") Integer threshold);

    @Modifying
    @Query("UPDATE Product p SET p.quantiteStock = p.quantiteStock - :quantity WHERE p.id = :productId AND p.quantiteStock >= :quantity")
    int decrementStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    @Modifying
    @Query("UPDATE Product p SET p.quantiteStock = p.quantiteStock + :quantity WHERE p.id = :productId")
    int incrementStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);
}
