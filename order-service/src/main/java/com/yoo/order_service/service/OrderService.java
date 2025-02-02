package com.yoo.order_service.service;

import com.yoo.order_service.dto.OrderDto;
import com.yoo.order_service.entity.OrderEntity;

import java.util.List;

public interface OrderService {
    OrderDto createOrder(OrderDto orderDetails);
    OrderDto getOrderByOrderId(String orderId);
    List<OrderEntity> getOrdersByUserId(String userId);
}
