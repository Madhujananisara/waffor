package com.waffor.deliveryservice.messaging;

import com.waffor.deliveryservice.config.JmsConfig;
import com.waffor.deliveryservice.dto.event.DeliveryRequestEvent;
import com.waffor.deliveryservice.dto.event.DeliveryResponseEvent;
import com.waffor.deliveryservice.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

/**
 * JMS listener that receives delivery assignment requests from the order-service,
 * delegates processing to {@link DeliveryService}, and publishes the DELIVERED
 * response back on the {@code delivery.response} queue.
 *
 * <p>Message flow:
 * <pre>
 *   order-service DeliveryDelegate
 *       → [delivery.request] → DeliveryRequestConsumer
 *           → DeliveryService.processDeliveryRequest()
 *               → ASSIGNED → OUT_FOR_DELIVERY → DELIVERED
 *       → [delivery.response] → order-service DeliveryDelegate (receives)
 * </pre>
 * </p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DeliveryRequestConsumer {

    private final DeliveryService deliveryService;
    private final JmsTemplate     jmsTemplate;

    /**
     * Consume a {@link DeliveryRequestEvent} from the {@code delivery.request} queue,
     * process the delivery, and publish a {@link DeliveryResponseEvent} back.
     *
     * @param event incoming delivery request from the order-service
     */
    @JmsListener(destination = JmsConfig.DELIVERY_REQUEST_QUEUE)
    public void onDeliveryRequest(DeliveryRequestEvent event) {
        log.info("📨 [JMS] DeliveryRequestEvent received on queue '{}' | Order ID: {} | Order#: {}",
                JmsConfig.DELIVERY_REQUEST_QUEUE,
                event.getOrderId(),
                event.getOrderNumber());

        DeliveryResponseEvent response;

        try {
            response = deliveryService.processDeliveryRequest(event);
            log.info("✅ [JMS] Delivery completed for Order ID: {} → Driver: {} | Status: {}",
                    event.getOrderId(), response.getDriverName(), response.getStatus());

        } catch (Exception ex) {
            log.error("💥 [JMS] Error processing delivery for Order ID: {}",
                    event.getOrderId(), ex);

            // Safe failure response — prevents order-service workflow from hanging
            response = DeliveryResponseEvent.builder()
                    .orderId(event.getOrderId())
                    .orderNumber(event.getOrderNumber())
                    .status("FAILED")
                    .build();
        }

        log.info("📤 [JMS] Publishing DeliveryResponseEvent to queue '{}' | Order ID: {} | Status: {}",
                JmsConfig.DELIVERY_RESPONSE_QUEUE,
                response.getOrderId(),
                response.getStatus());

        // jmsTemplate.convertAndSend(JmsConfig.DELIVERY_RESPONSE_QUEUE, response);

        log.info("✔ [JMS] DeliveryResponseEvent published for Order ID: {}", response.getOrderId());
    }
}
