package com.waffor.deliveryservice.controller;

import com.waffor.deliveryservice.dto.DeliveryResponseDto;
import com.waffor.deliveryservice.service.DeliveryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = DeliveryController.class)
public class DeliveryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DeliveryService deliveryService;

    @Test
    public void testCreateDelivery_Success() throws Exception {
        DeliveryResponseDto responseDto = DeliveryResponseDto.builder()
                .id(1L)
                .orderId(123L)
                .orderNumber("ORD-12345")
                .deliveryNumber("DLV-ORD-12345-TIMESTAMP")
                .driverId(1L)
                .driverName("Ravi Kumar")
                .deliveryAddress("Customer #456 Address, Waffor City")
                .status("DELIVERED")
                .deliveryTime(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        when(deliveryService.createDelivery(123L, "ORD-12345", 456L)).thenReturn(responseDto);

        mockMvc.perform(post("/api/delivery")
                        .param("orderId", "123")
                        .param("orderNumber", "ORD-12345")
                        .param("customerId", "456"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.orderId").value(123L))
                .andExpect(jsonPath("$.orderNumber").value("ORD-12345"))
                .andExpect(jsonPath("$.status").value("DELIVERED"))
                .andExpect(jsonPath("$.driverName").value("Ravi Kumar"));

        verify(deliveryService, times(1)).createDelivery(123L, "ORD-12345", 456L);
    }

    @Test
    public void testGetDelivery_Success() throws Exception {
        DeliveryResponseDto responseDto = DeliveryResponseDto.builder()
                .id(1L)
                .orderId(123L)
                .orderNumber("ORD-12345")
                .deliveryNumber("DLV-ORD-12345-TIMESTAMP")
                .driverId(1L)
                .driverName("Ravi Kumar")
                .deliveryAddress("Customer #456 Address, Waffor City")
                .status("DELIVERED")
                .deliveryTime(LocalDateTime.now())
                .build();

        when(deliveryService.getDeliveryByOrderId(123L)).thenReturn(responseDto);

        mockMvc.perform(get("/api/delivery/123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.orderId").value(123L))
                .andExpect(jsonPath("$.status").value("DELIVERED"));

        verify(deliveryService, times(1)).getDeliveryByOrderId(123L);
    }

    @Test
    public void testGetDelivery_NotFound() throws Exception {
        when(deliveryService.getDeliveryByOrderId(123L)).thenThrow(new IllegalArgumentException("No delivery found for Order ID: 123"));

        mockMvc.perform(get("/api/delivery/123"))
                .andExpect(status().isNotFound());

        verify(deliveryService, times(1)).getDeliveryByOrderId(123L);
    }

    @Test
    public void testHealthCheck() throws Exception {
        mockMvc.perform(get("/api/delivery/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.service").value("delivery-service"))
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.port").value("8084"));
    }
}
