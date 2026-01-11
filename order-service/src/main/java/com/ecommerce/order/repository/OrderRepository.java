package com.ecommerce.order.repository;

import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserIdOrderByDateCommandeDesc(String userId);

    List<Order> findByStatutOrderByDateCommandeDesc(OrderStatus statut);

    @Query("SELECT o FROM Order o ORDER BY o.dateCommande DESC")
    List<Order> findAllOrderByDateDesc();

    Optional<Order> findByIdAndUserId(Long id, String userId);

    @Query("SELECT o FROM Order o WHERE o.dateCommande BETWEEN :startDate AND :endDate ORDER BY o.dateCommande DESC")
    List<Order> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.userId = :userId")
    long countByUserId(@Param("userId") String userId);
}
