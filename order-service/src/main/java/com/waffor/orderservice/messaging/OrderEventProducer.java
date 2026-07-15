package com.waffor.orderservice.messaging;

import com.waffor.orderservice.config.JmsConfig;
import com.waffor.orderservice.dto.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer {

    private final JmsTemplate jmsTemplate;

    public void publishOrderCreated(OrderCreatedEvent event) {
        log.info("Publishing OrderCreatedEvent to queue '{}' for Order ID: {}", JmsConfig.ORDER_CREATED_QUEUE, event.getOrderId());
        try {
            jmsTemplate.convertAndSend(JmsConfig.ORDER_CREATED_QUEUE, event);
            log.info("Successfully published OrderCreatedEvent for Order ID: {}", event.getOrderId());
        } catch (Exception e) {
            log.error("Failed to publish OrderCreatedEvent for Order ID: {}", event.getOrderId(), e);
            throw e;
        }
    }
}
