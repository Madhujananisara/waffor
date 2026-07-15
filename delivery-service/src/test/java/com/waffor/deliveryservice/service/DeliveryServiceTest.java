package com.waffor.deliveryservice.service;

import com.waffor.deliveryservice.dto.DeliveryResponseDto;
import com.waffor.deliveryservice.dto.event.DeliveryRequestEvent;
import com.waffor.deliveryservice.dto.event.DeliveryResponseEvent;
import com.waffor.deliveryservice.entity.Delivery;
import com.waffor.deliveryservice.repository.DeliveryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DeliveryServiceTest {

    @Mock
    private DeliveryRepository deliveryRepository;

    @InjectMocks
    private DeliveryService deliveryService;

    @Test
    public void testProcessDeliveryRequest_Success() {
        DeliveryRequestEvent event = DeliveryRequestEvent.builder()
                .orderId(123L)
                .orderNumber("ORD-12345")
                .customerId(456L)
                .build();

        Delivery savedDelivery = Delivery.builder()
                .id(1L)
                .orderId(123L)
                .deliveryNumber("DLV-ORD-12345-TIMESTAMP")
                .driverId(1L)
                .deliveryAddress("Customer #456 Address, Waffor City")
                .status("DELIVERED")
                .deliveryTime(LocalDateTime.now())
                .build();

        when(deliveryRepository.save(any(Delivery.class))).thenReturn(savedDelivery);

        DeliveryResponseEvent response = deliveryService.processDeliveryRequest(event);

        assertNotNull(response);
        assertEquals(123L, response.getOrderId());
        assertEquals("ORD-12345", response.getOrderNumber());
        assertEquals("DELIVERED", response.getStatus());
        assertNotNull(response.getDriverName());
        assertNotNull(response.getDeliveryNumber());

        // Saves should happen 3 times (ASSIGNED, OUT_FOR_DELIVERY, DELIVERED)
        verify(deliveryRepository, times(3)).save(any(Delivery.class));
    }

    @Test
    public void testCreateDelivery_Success() {
        Delivery savedDelivery = Delivery.builder()
                .id(1L)
                .orderId(123L)
                .deliveryNumber("DLV-ORD-12345-TIMESTAMP")
                .driverId(1L)
                .deliveryAddress("Customer #456 Address, Waffor City")
                .status("DELIVERED")
                .deliveryTime(LocalDateTime.now())
                .build();

        when(deliveryRepository.save(any(Delivery.class))).thenReturn(savedDelivery);

        DeliveryResponseDto response = deliveryService.createDelivery(123L, "ORD-12345", 456L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(123L, response.getOrderId());
        assertEquals("ORD-12345", response.getOrderNumber());
        assertEquals("DELIVERED", response.getStatus());
        assertNotNull(response.getDriverName());

        verify(deliveryRepository, times(3)).save(any(Delivery.class));
    }

    @Test
    public void testGetDeliveryByOrderId_Success() {
        Delivery delivery = Delivery.builder()
                .id(1L)
                .orderId(123L)
                .deliveryNumber("DLV-ORD-12345-TIMESTAMP")
                .driverId(1L)
                .deliveryAddress("Customer #456 Address, Waffor City")
                .status("DELIVERED")
                .deliveryTime(LocalDateTime.now())
                .build();

        when(deliveryRepository.findByOrderId(123L)).thenReturn(Optional.of(delivery));

        DeliveryResponseDto response = deliveryService.getDeliveryByOrderId(123L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(123L, response.getOrderId());
        assertEquals("DELIVERED", response.getStatus());

        verify(deliveryRepository, times(1)).findByOrderId(123L);
    }

    @Test
    public void testGetDeliveryByOrderId_NotFound() {
        when(deliveryRepository.findByOrderId(123L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> deliveryService.getDeliveryByOrderId(123L));

        verify(deliveryRepository, times(1)).findByOrderId(123L);
    }
}
