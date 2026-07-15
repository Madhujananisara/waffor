package com.waffor.orderservice.service;

import com.waffor.orderservice.dto.OrderItemRequestDto;
import com.waffor.orderservice.dto.OrderRequestDto;
import com.waffor.orderservice.dto.OrderResponseDto;
import com.waffor.orderservice.dto.event.OrderCreatedEvent;
import com.waffor.orderservice.entity.Order;
import com.waffor.orderservice.exception.ResourceNotFoundException;
import com.waffor.orderservice.messaging.OrderEventProducer;
import com.waffor.orderservice.repository.OrderRepository;
import com.waffor.orderservice.repository.OfferRepository;
import com.waffor.orderservice.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderEventProducer orderEventProducer;

    @Mock
    private OfferRepository offerRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    public void testCreateOrder_Success() {
        OrderRequestDto requestDto = OrderRequestDto.builder()
                .customerId(101L)
                .items(List.of(
                        OrderItemRequestDto.builder()
                                .productId(201L)
                                .quantity(3)
                                .price(BigDecimal.valueOf(10.00))
                                .build()
                ))
                .build();

        Order savedOrder = Order.builder()
                .id(1L)
                .orderNumber("ORD-ABCDE")
                .customerId(101L)
                .status("PLACED")
                .totalAmount(BigDecimal.valueOf(30.00))
                .build();

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(offerRepository.findByActive(true)).thenReturn(List.of());
        doNothing().when(orderEventProducer).publishOrderCreated(any(OrderCreatedEvent.class));

        OrderResponseDto response = orderService.createOrder(requestDto);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("ORD-ABCDE", response.getOrderNumber());
        assertEquals("PLACED", response.getStatus());
        assertEquals(0, BigDecimal.valueOf(30.00).compareTo(response.getTotalAmount()));

        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderEventProducer, times(1)).publishOrderCreated(any(OrderCreatedEvent.class));
    }

    @Test
    public void testGetOrderById_Success() {
        Order savedOrder = Order.builder()
                .id(1L)
                .orderNumber("ORD-ABCDE")
                .customerId(101L)
                .status("PLACED")
                .totalAmount(BigDecimal.valueOf(30.00))
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(savedOrder));

        OrderResponseDto response = orderService.getOrderById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("ORD-ABCDE", response.getOrderNumber());

        verify(orderRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetOrderById_NotFound() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.getOrderById(1L));

        verify(orderRepository, times(1)).findById(1L);
    }
}
