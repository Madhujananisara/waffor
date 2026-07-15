package com.waffor.deliveryservice.messaging;

import com.waffor.deliveryservice.config.JmsConfig;
import com.waffor.deliveryservice.dto.event.DeliveryRequestEvent;
import com.waffor.deliveryservice.dto.event.DeliveryResponseEvent;
import com.waffor.deliveryservice.service.DeliveryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DeliveryRequestConsumerTest {

    @Mock
    private DeliveryService deliveryService;

    @Mock
    private JmsTemplate jmsTemplate;

    @InjectMocks
    private DeliveryRequestConsumer deliveryRequestConsumer;

    @Test
    public void testOnDeliveryRequest_Success() {
        DeliveryRequestEvent requestEvent = DeliveryRequestEvent.builder()
                .orderId(123L)
                .orderNumber("ORD-12345")
                .customerId(456L)
                .build();

        DeliveryResponseEvent successResponse = DeliveryResponseEvent.builder()
                .orderId(123L)
                .orderNumber("ORD-12345")
                .deliveryNumber("DLV-ORD-12345-TIMESTAMP")
                .driverId(1L)
                .driverName("Ravi Kumar")
                .deliveryAddress("Customer #456 Address, Waffor City")
                .status("DELIVERED")
                .build();

        when(deliveryService.processDeliveryRequest(requestEvent)).thenReturn(successResponse);

        deliveryRequestConsumer.onDeliveryRequest(requestEvent);

        verify(deliveryService, times(1)).processDeliveryRequest(requestEvent);

        ArgumentCaptor<DeliveryResponseEvent> captor = ArgumentCaptor.forClass(DeliveryResponseEvent.class);
        verify(jmsTemplate, times(1)).convertAndSend(eq(JmsConfig.DELIVERY_RESPONSE_QUEUE), captor.capture());

        DeliveryResponseEvent sentResponse = captor.getValue();
        assertNotNull(sentResponse);
        assertEquals(123L, sentResponse.getOrderId());
        assertEquals("ORD-12345", sentResponse.getOrderNumber());
        assertEquals("DELIVERED", sentResponse.getStatus());
        assertEquals("Ravi Kumar", sentResponse.getDriverName());
    }

    @Test
    public void testOnDeliveryRequest_Failure() {
        DeliveryRequestEvent requestEvent = DeliveryRequestEvent.builder()
                .orderId(123L)
                .orderNumber("ORD-12345")
                .customerId(456L)
                .build();

        when(deliveryService.processDeliveryRequest(requestEvent)).thenThrow(new RuntimeException("JMS error"));

        deliveryRequestConsumer.onDeliveryRequest(requestEvent);

        verify(deliveryService, times(1)).processDeliveryRequest(requestEvent);

        ArgumentCaptor<DeliveryResponseEvent> captor = ArgumentCaptor.forClass(DeliveryResponseEvent.class);
        verify(jmsTemplate, times(1)).convertAndSend(eq(JmsConfig.DELIVERY_RESPONSE_QUEUE), captor.capture());

        DeliveryResponseEvent sentResponse = captor.getValue();
        assertNotNull(sentResponse);
        assertEquals(123L, sentResponse.getOrderId());
        assertEquals("ORD-12345", sentResponse.getOrderNumber());
        assertEquals("FAILED", sentResponse.getStatus());
        assertNull(sentResponse.getDriverName());
    }
}
