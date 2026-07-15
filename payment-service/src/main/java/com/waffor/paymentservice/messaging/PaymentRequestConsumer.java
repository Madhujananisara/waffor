package com.waffor.paymentservice.messaging;

import com.waffor.paymentservice.config.JmsConfig;
import com.waffor.paymentservice.dto.event.PaymentRequestEvent;
import com.waffor.paymentservice.dto.event.PaymentResponseEvent;
import com.waffor.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

/**
 * JMS listener that receives payment requests from the order-service,
 * delegates processing to {@link PaymentService}, and sends the result
 * back on the {@code payment.response} queue.
 *
 * <p>The order-service's {@code PaymentDelegate} publishes a
 * {@link PaymentRequestEvent} to {@code payment.request}, and this
 * consumer picks it up, processes it, then publishes a
 * {@link PaymentResponseEvent} to {@code payment.response}.</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentRequestConsumer {

    private final PaymentService paymentService;
    private final JmsTemplate    jmsTemplate;

    /**
     * Consume a {@link PaymentRequestEvent} from the {@code payment.request} queue,
     * process the payment, and publish the response to {@code payment.response}.
     *
     * @param event incoming payment request from order-service
     */
    @JmsListener(destination = JmsConfig.PAYMENT_REQUEST_QUEUE)
    public void onPaymentRequest(PaymentRequestEvent event) {
        log.info("📨 [JMS] Received PaymentRequestEvent from queue '{}' | Order ID: {} | Order#: {} | Amount: {}",
                JmsConfig.PAYMENT_REQUEST_QUEUE,
                event.getOrderId(),
                event.getOrderNumber(),
                event.getTotalAmount());

        PaymentResponseEvent response;

        try {
            response = paymentService.processPaymentEvent(event);
            log.info("✅ [JMS] Payment processed successfully for Order ID: {} → Status: {}",
                    event.getOrderId(), response.getStatus());
        } catch (Exception ex) {
            log.error("💥 [JMS] Error processing payment for Order ID: {}", event.getOrderId(), ex);

            // Build a FAILED response so the order-service workflow is not stuck
            response = PaymentResponseEvent.builder()
                    .orderId(event.getOrderId())
                    .orderNumber(event.getOrderNumber())
                    .amount(event.getTotalAmount())
                    .status("FAILED")
                    .failureReason("Internal payment-service error: " + ex.getMessage())
                    .build();
        }

        // Send result back to order-service
        log.info("📤 [JMS] Publishing PaymentResponseEvent to queue '{}' | Order ID: {} | Status: {}",
                JmsConfig.PAYMENT_RESPONSE_QUEUE,
                response.getOrderId(),
                response.getStatus());

        // jmsTemplate.convertAndSend(JmsConfig.PAYMENT_RESPONSE_QUEUE, response);

        log.info("✔ [JMS] PaymentResponseEvent published for Order ID: {}", response.getOrderId());
    }
}
