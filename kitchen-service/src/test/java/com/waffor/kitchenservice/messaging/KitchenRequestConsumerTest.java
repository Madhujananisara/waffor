package com.waffor.kitchenservice.messaging;

import com.waffor.kitchenservice.config.JmsConfig;
import com.waffor.kitchenservice.dto.event.KitchenRequestEvent;
import com.waffor.kitchenservice.dto.event.KitchenResponseEvent;
import com.waffor.kitchenservice.service.KitchenService;
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
public class KitchenRequestConsumerTest {

    @Mock
    private KitchenService kitchenService;

    @Mock
    private JmsTemplate jmsTemplate;

    @InjectMocks
    private KitchenRequestConsumer kitchenRequestConsumer;

    @Test
    public void testOnKitchenRequest_Success() {
        KitchenRequestEvent requestEvent = KitchenRequestEvent.builder()
                .orderId(123L)
                .orderNumber("ORD-12345")
                .customerId(456L)
                .build();

        KitchenResponseEvent successResponse = KitchenResponseEvent.builder()
                .orderId(123L)
                .orderNumber("ORD-12345")
                .ticketNumber("KT-ORD-12345-123")
                .status("READY")
                .estimatedPreparationTime(15)
                .build();

        when(kitchenService.processKitchenRequest(requestEvent)).thenReturn(successResponse);

        kitchenRequestConsumer.onKitchenRequest(requestEvent);

        verify(kitchenService, times(1)).processKitchenRequest(requestEvent);

        ArgumentCaptor<KitchenResponseEvent> captor = ArgumentCaptor.forClass(KitchenResponseEvent.class);
        verify(jmsTemplate, times(1)).convertAndSend(eq(JmsConfig.KITCHEN_RESPONSE_QUEUE), captor.capture());

        KitchenResponseEvent sentResponse = captor.getValue();
        assertNotNull(sentResponse);
        assertEquals(123L, sentResponse.getOrderId());
        assertEquals("ORD-12345", sentResponse.getOrderNumber());
        assertEquals("READY", sentResponse.getStatus());
        assertEquals("KT-ORD-12345-123", sentResponse.getTicketNumber());
        assertEquals(15, sentResponse.getEstimatedPreparationTime());
    }

    @Test
    public void testOnKitchenRequest_Failure() {
        KitchenRequestEvent requestEvent = KitchenRequestEvent.builder()
                .orderId(123L)
                .orderNumber("ORD-12345")
                .customerId(456L)
                .build();

        when(kitchenService.processKitchenRequest(requestEvent)).thenThrow(new RuntimeException("Database error"));

        kitchenRequestConsumer.onKitchenRequest(requestEvent);

        verify(kitchenService, times(1)).processKitchenRequest(requestEvent);

        ArgumentCaptor<KitchenResponseEvent> captor = ArgumentCaptor.forClass(KitchenResponseEvent.class);
        verify(jmsTemplate, times(1)).convertAndSend(eq(JmsConfig.KITCHEN_RESPONSE_QUEUE), captor.capture());

        KitchenResponseEvent sentResponse = captor.getValue();
        assertNotNull(sentResponse);
        assertEquals(123L, sentResponse.getOrderId());
        assertEquals("ORD-12345", sentResponse.getOrderNumber());
        assertEquals("FAILED", sentResponse.getStatus());
        assertEquals(0, sentResponse.getEstimatedPreparationTime());
        assertNull(sentResponse.getTicketNumber());
    }
}
