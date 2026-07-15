package com.waffor.orderservice.messaging;

import com.waffor.orderservice.config.JmsConfig;
import com.waffor.orderservice.dto.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    private final RuntimeService runtimeService;

    @JmsListener(destination = JmsConfig.ORDER_CREATED_QUEUE)
    public void consumeOrderCreated(OrderCreatedEvent event) {
        log.info("Received OrderCreatedEvent from ActiveMQ for Order ID: {} (Number: {})", 
                event.getOrderId(), event.getOrderNumber());

        try {
            // Define workflow variables
            Map<String, Object> variables = new HashMap<>();
            variables.put("orderId", event.getOrderId());
            variables.put("orderNumber", event.getOrderNumber());
            variables.put("customerId", event.getCustomerId());
            variables.put("totalAmount", event.getTotalAmount() != null ? event.getTotalAmount().doubleValue() : 0.0);

            // Start the process asynchronously in the background
            log.info("Starting Camunda workflow 'order-processing' for Order ID: {}", event.getOrderId());
            runtimeService.startProcessInstanceByKey("order-processing", variables);
            log.info("Successfully started Camunda process instance for Order ID: {}", event.getOrderId());
        } catch (Exception e) {
            log.error("Failed to start Camunda workflow for Order ID: {}", event.getOrderId(), e);
            throw e;
        }
    }
}
