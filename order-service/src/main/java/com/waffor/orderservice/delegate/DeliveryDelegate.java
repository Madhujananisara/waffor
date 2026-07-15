package com.waffor.orderservice.delegate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waffor.orderservice.config.JmsConfig;
import com.waffor.orderservice.dto.event.DeliveryRequestEvent;
import com.waffor.orderservice.dto.event.DeliveryResponseEvent;
import com.waffor.orderservice.entity.Order;
import com.waffor.orderservice.repository.OrderRepository;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

/**
 * Camunda JavaDelegate that assigns a delivery driver and dispatches an order.
 *
 * <p>Flow:
 * <ol>
 *   <li>Publishes a {@link DeliveryRequestEvent} to {@code delivery.request}.</li>
 *   <li>Blocks (up to {@code JMS_RECEIVE_TIMEOUT_MS}) for a
 *       {@link DeliveryResponseEvent} on {@code delivery.response}.</li>
 *   <li>Updates Order status to {@code DELIVERED} on success,
 *       {@code DELIVERY_FAILED} on timeout.</li>
 *   <li>Sets Camunda variables: {@code deliveryNumber}, {@code driverName},
 *       {@code deliveryAddress}.</li>
 * </ol>
 * </p>
 */
@Component("deliveryDelegate")
@RequiredArgsConstructor
@Slf4j
public class DeliveryDelegate implements JavaDelegate {

    /** Timeout for waiting on the delivery response queue (60 seconds). */
    private static final long JMS_RECEIVE_TIMEOUT_MS = 300_000L;

    private final OrderRepository orderRepository;
    private final JmsTemplate     jmsTemplate;
    private final ObjectMapper    objectMapper;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        Long   orderId     = (Long)   execution.getVariable("orderId");
        String orderNumber = (String) execution.getVariable("orderNumber");
        Long   customerId  = (Long)   execution.getVariable("customerId");

        log.info("🔵 [DeliveryDelegate] Starting for Order ID: {} | Order#: {}", orderId, orderNumber);

        // ---- 1. Build and publish delivery request ----
        updateOrderStatus(orderId, "OUT_FOR_DELIVERY");

        DeliveryRequestEvent request = DeliveryRequestEvent.builder()
                .orderId(orderId)
                .orderNumber(orderNumber)
                .customerId(customerId)
                .build();

        log.info("📤 [DeliveryDelegate] Publishing DeliveryRequestEvent to queue '{}'",
                JmsConfig.DELIVERY_REQUEST_QUEUE);
        jmsTemplate.convertAndSend(JmsConfig.DELIVERY_REQUEST_QUEUE, request);

        // ---- 2. Wait for DELIVERED response from delivery-service ----
        log.info("⏳ [DeliveryDelegate] Waiting for DeliveryResponseEvent on queue '{}' (timeout: {}ms)",
                JmsConfig.DELIVERY_RESPONSE_QUEUE, JMS_RECEIVE_TIMEOUT_MS);

        jmsTemplate.setReceiveTimeout(JMS_RECEIVE_TIMEOUT_MS);
        Message rawMessage = jmsTemplate.receive(JmsConfig.DELIVERY_RESPONSE_QUEUE);

        if (rawMessage == null) {
            log.error("⏰ [DeliveryDelegate] Timed out waiting for delivery response for Order ID: {}", orderId);
            updateOrderStatus(orderId, "DELIVERY_FAILED");
            return;
        }

        // ---- 3. Deserialize response ----
        String json = ((TextMessage) rawMessage).getText();
        log.debug("[DeliveryDelegate] Raw delivery response JSON: {}", json);
        DeliveryResponseEvent response = objectMapper.readValue(json, DeliveryResponseEvent.class);

        log.info("📨 [DeliveryDelegate] DeliveryResponseEvent received | Order ID: {} → Status: {} | Driver: {} | DeliveryNumber: {}",
                orderId, response.getStatus(), response.getDriverName(), response.getDeliveryNumber());

        // ---- 4. Update order status & set Camunda variables ----
        boolean isDelivered = "DELIVERED".equalsIgnoreCase(response.getStatus());
        execution.setVariable("deliveryNumber",  response.getDeliveryNumber());
        execution.setVariable("driverName",      response.getDriverName());
        execution.setVariable("deliveryAddress", response.getDeliveryAddress());

        updateOrderStatus(orderId, isDelivered ? "DELIVERED" : "DELIVERY_FAILED");
        log.info("[OrderService] Workflow COMPLETE");
    }


    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private void updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + orderId));
        order.setStatus(status);
        orderRepository.save(order);
        log.info("💾 [DeliveryDelegate] Order ID: {} status saved: {}", orderId, status);
    }
}

