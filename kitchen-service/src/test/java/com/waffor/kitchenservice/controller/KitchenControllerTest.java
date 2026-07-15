package com.waffor.kitchenservice.controller;

import com.waffor.kitchenservice.dto.KitchenTicketResponseDto;
import com.waffor.kitchenservice.service.KitchenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = KitchenController.class)
public class KitchenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private KitchenService kitchenService;

    @Test
    public void testCreateTicket_Success() throws Exception {
        KitchenTicketResponseDto responseDto = KitchenTicketResponseDto.builder()
                .id(1L)
                .orderId(123L)
                .orderNumber("ORD-12345")
                .ticketNumber("KT-ORD-12345-TIMESTAMP")
                .status("READY")
                .estimatedPreparationTime(15)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(kitchenService.createKitchenTicket(123L, "ORD-12345")).thenReturn(responseDto);

        mockMvc.perform(post("/api/kitchen/tickets")
                        .param("orderId", "123")
                        .param("orderNumber", "ORD-12345"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.orderId").value(123L))
                .andExpect(jsonPath("$.orderNumber").value("ORD-12345"))
                .andExpect(jsonPath("$.status").value("READY"))
                .andExpect(jsonPath("$.ticketNumber").value("KT-ORD-12345-TIMESTAMP"));

        verify(kitchenService, times(1)).createKitchenTicket(123L, "ORD-12345");
    }

    @Test
    public void testGetTicketByOrderId_Success() throws Exception {
        KitchenTicketResponseDto responseDto = KitchenTicketResponseDto.builder()
                .id(1L)
                .orderId(123L)
                .orderNumber("ORD-12345")
                .ticketNumber("KT-ORD-12345-TIMESTAMP")
                .status("READY")
                .estimatedPreparationTime(15)
                .build();

        when(kitchenService.getTicketByOrderId(123L)).thenReturn(responseDto);

        mockMvc.perform(get("/api/kitchen/tickets/123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.orderId").value(123L))
                .andExpect(jsonPath("$.status").value("READY"));

        verify(kitchenService, times(1)).getTicketByOrderId(123L);
    }

    @Test
    public void testGetTicketByOrderId_NotFound() throws Exception {
        when(kitchenService.getTicketByOrderId(123L)).thenThrow(new IllegalArgumentException("No kitchen ticket found for Order ID: 123"));

        mockMvc.perform(get("/api/kitchen/tickets/123"))
                .andExpect(status().isNotFound());

        verify(kitchenService, times(1)).getTicketByOrderId(123L);
    }

    @Test
    public void testHealthCheck() throws Exception {
        mockMvc.perform(get("/api/kitchen/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.service").value("kitchen-service"))
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.port").value("8083"));
    }
}
