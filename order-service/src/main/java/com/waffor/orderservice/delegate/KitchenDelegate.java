package com.waffor.orderservice.delegate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waffor.orderservice.config.JmsConfig;
import com.waffor.orderservice.dto.event.KitchenRequestEvent;
import com.waffor.orderservice.dto.event.KitchenResponseEvent;
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
 * Camunda JavaDelegate that sends an order to the kitchen-service for preparation.
 *
 * <p>Flow:
 * <ol>
 *   <li>Publishes a {@link KitchenRequestEvent} to {@code kitchen.request}.</li>
 *   <li>Blocks (up to {@code JMS_RECEIVE_TIMEOUT_MS}) for a
 *       {@link KitchenResponseEvent} on {@code kitchen.response}.</li>
 *   <li>Updates Order status to {@code READY} on success, {@code KITCHEN_FAILED} on timeout.</li>
 *   <li>Sets the Camunda variable {@code kitchenReady} for downstream gateway use.</li>
 * </ol>
 * </p>
 */
@Component("kitchenDelegate")
@RequiredArgsConstructor
@Slf4j
public class KitchenDelegate implements JavaDelegate {

    /** Timeout for waiting on the kitchen response queue (60 seconds). */
    private static final long JMS_RECEIVE_TIMEOUT_MS = 300_000L;

    private final OrderRepository orderRepository;
    private final JmsTemplate     jmsTemplate;
    private final ObjectMapper    objectMapper;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        Long   orderId     = (Long)   execution.getVariable("orderId");
        String orderNumber = (String) execution.getVariable("orderNumber");
        Long   customerId  = (Long)   execution.getVariable("customerId");

        log.info("🔵 [KitchenDelegate] Starting for Order ID: {} | Order#: {}", orderId, orderNumber);

        // ---- 1. Build and publish kitchen request ----
        updateOrderStatus(orderId, "PREPARING");

        KitchenRequestEvent request = KitchenRequestEvent.builder()
                .orderId(orderId)
                .orderNumber(orderNumber)
                .customerId(customerId)
                .build();

        log.info("📤 [KitchenDelegate] Publishing KitchenRequestEvent to queue '{}'",
                JmsConfig.KITCHEN_REQUEST_QUEUE);
        jmsTemplate.convertAndSend(JmsConfig.KITCHEN_REQUEST_QUEUE, request);

        // ---- 2. Wait for READY response from kitchen-service ----
        log.info("⏳ [KitchenDelegate] Waiting for KitchenResponseEvent on queue '{}' (timeout: {}ms)",
                JmsConfig.KITCHEN_RESPONSE_QUEUE, JMS_RECEIVE_TIMEOUT_MS);

        jmsTemplate.setReceiveTimeout(JMS_RECEIVE_TIMEOUT_MS);
        Message rawMessage = jmsTemplate.receive(JmsConfig.KITCHEN_RESPONSE_QUEUE);

        if (rawMessage == null) {
            log.error("⏰ [KitchenDelegate] Timed out waiting for kitchen response for Order ID: {}", orderId);
            updateOrderStatus(orderId, "KITCHEN_FAILED");
            execution.setVariable("kitchenReady", false);
            return;
        }

        // ---- 3. Deserialize response ----
        String json = ((TextMessage) rawMessage).getText();
        log.debug("[KitchenDelegate] Raw kitchen response JSON: {}", json);
        KitchenResponseEvent response = objectMapper.readValue(json, KitchenResponseEvent.class);

        log.info("📨 [KitchenDelegate] Received KitchenResponseEvent | Order ID: {} → Status: {} | Ticket#: {}",
                orderId, response.getStatus(), response.getTicketNumber());

        // ---- 4. Update order status & Camunda variables ----
        boolean isReady = "READY".equalsIgnoreCase(response.getStatus());
        execution.setVariable("kitchenReady",    isReady);
        execution.setVariable("ticketNumber",    response.getTicketNumber());
        execution.setVariable("estimatedPrepTime", response.getEstimatedPreparationTime());

        updateOrderStatus(orderId, isReady ? "READY" : "KITCHEN_FAILED");
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private void updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + orderId));
        order.setStatus(status);
        orderRepository.save(order);
        log.info("💾 [KitchenDelegate] Order ID: {} status updated to {}", orderId, status);
    }
}

