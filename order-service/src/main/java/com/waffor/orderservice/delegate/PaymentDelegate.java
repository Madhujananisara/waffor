package com.waffor.orderservice.delegate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waffor.orderservice.config.JmsConfig;
import com.waffor.orderservice.dto.event.PaymentRequestEvent;
import com.waffor.orderservice.dto.event.PaymentResponseEvent;
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

import java.math.BigDecimal;

/**
 * Camunda JavaDelegate that processes payment for an order.
 *
 * <p>Flow:
 * <ol>
 *   <li>Publishes a {@link PaymentRequestEvent} to the {@code payment.request} queue.</li>
 *   <li>Blocks (up to {@code JMS_RECEIVE_TIMEOUT_MS}) waiting for a
 *       {@link PaymentResponseEvent} on the {@code payment.response} queue.</li>
 *   <li>Updates the Order status ({@code PAID} / {@code PAYMENT_FAILED}) in the DB.</li>
 *   <li>Sets the Camunda process variable {@code paymentSuccess} used by the BPMN gateway.</li>
 * </ol>
 * </p>
 */
@Component("paymentDelegate")
@RequiredArgsConstructor
@Slf4j
public class PaymentDelegate implements JavaDelegate {

    /** Timeout for waiting on the payment response queue (30 seconds). */
    private static final long JMS_RECEIVE_TIMEOUT_MS = 300_000L;

    private final OrderRepository orderRepository;
    private final JmsTemplate     jmsTemplate;
    private final ObjectMapper    objectMapper;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        Long   orderId     = (Long)   execution.getVariable("orderId");
        String orderNumber = (String) execution.getVariable("orderNumber");
        Double totalAmount = (Double) execution.getVariable("totalAmount");

        log.info("🔵 [PaymentDelegate] Starting for Order ID: {} | Order#: {} | Amount: {}",
                orderId, orderNumber, totalAmount);

        // ---- 1. Build and publish payment request ----
        PaymentRequestEvent request = PaymentRequestEvent.builder()
                .orderId(orderId)
                .orderNumber(orderNumber)
                .totalAmount(totalAmount != null
                        ? BigDecimal.valueOf(totalAmount)
                        : BigDecimal.ZERO)
                .build();

        log.info("📤 [PaymentDelegate] Publishing PaymentRequestEvent to queue '{}'", JmsConfig.PAYMENT_REQUEST_QUEUE);
        jmsTemplate.convertAndSend(JmsConfig.PAYMENT_REQUEST_QUEUE, request);

        // ---- 2. Wait for response from payment-service ----
        log.info("⏳ [PaymentDelegate] Waiting for PaymentResponseEvent on queue '{}' (timeout: {}ms)",
                JmsConfig.PAYMENT_RESPONSE_QUEUE, JMS_RECEIVE_TIMEOUT_MS);

        jmsTemplate.setReceiveTimeout(JMS_RECEIVE_TIMEOUT_MS);
        Message rawMessage = jmsTemplate.receive(JmsConfig.PAYMENT_RESPONSE_QUEUE);

        if (rawMessage == null) {
            log.error("⏰ [PaymentDelegate] Timed out waiting for payment response for Order ID: {}", orderId);
            handlePaymentOutcome(execution, orderId, false);
            return;
        }

        // ---- 3. Deserialize response ----
        String json = ((TextMessage) rawMessage).getText();
        log.debug("[PaymentDelegate] Raw payment response JSON: {}", json);
        PaymentResponseEvent response = objectMapper.readValue(json, PaymentResponseEvent.class);

        log.info("📨 [PaymentDelegate] Received PaymentResponseEvent for Order ID: {} → Status: {} | TxnID: {}",
                orderId, response.getStatus(), response.getTransactionId());

        // ---- 4. Update order status & set Camunda variable ----
        boolean isSuccess = "SUCCESS".equalsIgnoreCase(response.getStatus());
        execution.setVariable("paymentSuccess",    isSuccess);
        execution.setVariable("paymentNumber",     response.getPaymentNumber());
        execution.setVariable("transactionId",     response.getTransactionId());

        handlePaymentOutcome(execution, orderId, isSuccess);
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private void handlePaymentOutcome(DelegateExecution execution, Long orderId, boolean isSuccess) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + orderId));

        if (isSuccess) {
            log.info("✅ [PaymentDelegate] Payment SUCCESS → updating Order ID: {} to PAID", orderId);
            order.setStatus("PAID");
        } else {
            log.warn("❌ [PaymentDelegate] Payment FAILED → updating Order ID: {} to PAYMENT_FAILED", orderId);
            order.setStatus("PAYMENT_FAILED");
            execution.setVariable("paymentSuccess", false);
        }

        orderRepository.save(order);
        log.info("💾 [PaymentDelegate] Order ID: {} status saved: {}", orderId, order.getStatus());
    }
}

