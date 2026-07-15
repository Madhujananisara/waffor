package com.waffor.orderservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waffor.orderservice.dto.OrderItemRequestDto;
import com.waffor.orderservice.dto.OrderRequestDto;
import com.waffor.orderservice.dto.OrderResponseDto;
import com.waffor.orderservice.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = OrderController.class,
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        org.camunda.bpm.spring.boot.starter.CamundaBpmAutoConfiguration.class
    }
)
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @Test
    public void testCreateOrder() throws Exception {
        OrderRequestDto requestDto = OrderRequestDto.builder()
                .customerId(101L)
                .items(List.of(
                        OrderItemRequestDto.builder()
                                .productId(201L)
                                .quantity(2)
                                .price(BigDecimal.valueOf(15.50))
                                .build()
                ))
                .build();

        OrderResponseDto responseDto = OrderResponseDto.builder()
                .id(1L)
                .orderNumber("ORD-12345")
                .customerId(101L)
                .status("PLACED")
                .totalAmount(BigDecimal.valueOf(31.00))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .items(List.of())
                .build();

        when(orderService.createOrder(any(OrderRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.orderNumber").value("ORD-12345"))
                .andExpect(jsonPath("$.status").value("PLACED"))
                .andExpect(jsonPath("$.totalAmount").value(31.00));
    }

    @Test
    public void testGetAllOrders() throws Exception {
        OrderResponseDto responseDto = OrderResponseDto.builder()
                .id(1L)
                .orderNumber("ORD-12345")
                .customerId(101L)
                .status("PLACED")
                .totalAmount(BigDecimal.valueOf(31.00))
                .items(List.of())
                .build();

        when(orderService.getAllOrders()).thenReturn(List.of(responseDto));

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].orderNumber").value("ORD-12345"));
    }

    @Test
    public void testGetOrderById() throws Exception {
        OrderResponseDto responseDto = OrderResponseDto.builder()
                .id(1L)
                .orderNumber("ORD-12345")
                .customerId(101L)
                .status("PLACED")
                .totalAmount(BigDecimal.valueOf(31.00))
                .items(List.of())
                .build();

        when(orderService.getOrderById(1L)).thenReturn(responseDto);

        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.orderNumber").value("ORD-12345"));
    }
}
