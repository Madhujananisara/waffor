package com.waffor.kitchenservice.service;

import com.waffor.kitchenservice.dto.KitchenTicketResponseDto;
import com.waffor.kitchenservice.dto.event.KitchenRequestEvent;
import com.waffor.kitchenservice.dto.event.KitchenResponseEvent;
import com.waffor.kitchenservice.entity.KitchenTicket;
import com.waffor.kitchenservice.repository.KitchenTicketRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class KitchenServiceTest {

    @Mock
    private KitchenTicketRepository kitchenTicketRepository;

    @InjectMocks
    private KitchenService kitchenService;

    @Test
    public void testProcessKitchenRequest_Success() {
        KitchenRequestEvent event = KitchenRequestEvent.builder()
                .orderId(123L)
                .orderNumber("ORD-12345")
                .customerId(456L)
                .build();

        KitchenTicket savedTicket = KitchenTicket.builder()
                .id(1L)
                .orderId(123L)
                .ticketNumber("KT-ORD-12345-TIMESTAMP")
                .status("READY")
                .estimatedPreparationTime(15)
                .build();

        when(kitchenTicketRepository.save(any(KitchenTicket.class))).thenReturn(savedTicket);

        KitchenResponseEvent response = kitchenService.processKitchenRequest(event);

        assertNotNull(response);
        assertEquals(123L, response.getOrderId());
        assertEquals("ORD-12345", response.getOrderNumber());
        assertEquals("READY", response.getStatus());
        assertEquals(15, response.getEstimatedPreparationTime());
        assertNotNull(response.getTicketNumber());

        // Saves should happen 3 times (RECEIVED, PREPARING, READY)
        verify(kitchenTicketRepository, times(3)).save(any(KitchenTicket.class));
    }

    @Test
    public void testCreateKitchenTicket_Success() {
        KitchenTicket savedTicket = KitchenTicket.builder()
                .id(1L)
                .orderId(123L)
                .ticketNumber("KT-ORD-12345-TIMESTAMP")
                .status("READY")
                .estimatedPreparationTime(15)
                .build();

        when(kitchenTicketRepository.save(any(KitchenTicket.class))).thenReturn(savedTicket);

        KitchenTicketResponseDto response = kitchenService.createKitchenTicket(123L, "ORD-12345");

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(123L, response.getOrderId());
        assertEquals("ORD-12345", response.getOrderNumber());
        assertEquals("READY", response.getStatus());
        assertEquals(15, response.getEstimatedPreparationTime());

        verify(kitchenTicketRepository, times(3)).save(any(KitchenTicket.class));
    }

    @Test
    public void testGetTicketByOrderId_Success() {
        KitchenTicket ticket = KitchenTicket.builder()
                .id(1L)
                .orderId(123L)
                .ticketNumber("KT-ORD-12345-TIMESTAMP")
                .status("READY")
                .estimatedPreparationTime(15)
                .build();

        when(kitchenTicketRepository.findByOrderId(123L)).thenReturn(Optional.of(ticket));

        KitchenTicketResponseDto response = kitchenService.getTicketByOrderId(123L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(123L, response.getOrderId());
        assertEquals("READY", response.getStatus());

        verify(kitchenTicketRepository, times(1)).findByOrderId(123L);
    }

    @Test
    public void testGetTicketByOrderId_NotFound() {
        when(kitchenTicketRepository.findByOrderId(123L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> kitchenService.getTicketByOrderId(123L));

        verify(kitchenTicketRepository, times(1)).findByOrderId(123L);
    }
}
