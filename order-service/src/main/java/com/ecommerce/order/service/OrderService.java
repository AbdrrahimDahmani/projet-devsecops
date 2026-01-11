package com.ecommerce.order.service;

import com.ecommerce.order.dto.CreateOrderRequest;
import com.ecommerce.order.dto.OrderDTO;

import java.util.List;

public interface OrderService {

    OrderDTO createOrder(CreateOrderRequest request, String userId, String username, String token);

    OrderDTO getOrderById(Long orderId, String userId, boolean isAdmin);

    List<OrderDTO> getMyOrders(String userId);

    List<OrderDTO> getAllOrders();

    OrderDTO cancelOrder(Long orderId, String userId, boolean isAdmin, String token);
}
