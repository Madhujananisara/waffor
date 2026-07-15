package com.waffor.orderservice.service;

import com.waffor.orderservice.dto.OrderRequestDto;
import com.waffor.orderservice.dto.OrderResponseDto;

import java.util.List;

public interface OrderService {
    OrderResponseDto createOrder(OrderRequestDto request);
    List<OrderResponseDto> getAllOrders();
    OrderResponseDto getOrderById(Long id);
    OrderResponseDto updateOrderStatus(Long id, String status);
}
