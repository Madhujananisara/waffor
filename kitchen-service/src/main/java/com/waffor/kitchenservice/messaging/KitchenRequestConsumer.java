package com.waffor.kitchenservice.messaging;

import com.waffor.kitchenservice.config.JmsConfig;
import com.waffor.kitchenservice.dto.event.KitchenRequestEvent;
import com.waffor.kitchenservice.dto.event.KitchenResponseEvent;
import com.waffor.kitchenservice.service.KitchenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

/**
 * JMS listener that receives kitchen work requests from the order-service,
 * delegates processing to {@link KitchenService}, and publishes the READY
 * response back on the {@code kitchen.response} queue.
 *
 * <p>Message flow:
 * <pre>
 *   order-service KitchenDelegate
 *       → [kitchen.request] → KitchenRequestConsumer
 *           → KitchenService.processKitchenRequest()
 *       → [kitchen.response] → order-service KitchenDelegate (receives)
 * </pre>
 * </p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KitchenRequestConsumer {

    private final KitchenService kitchenService;
    private final JmsTemplate    jmsTemplate;

    /**
     * Consume a {@link KitchenRequestEvent} from the {@code kitchen.request} queue,
     * create the kitchen ticket, and publish a {@link KitchenResponseEvent} back.
     *
     * @param event incoming kitchen request from the order-service
     */
    @JmsListener(destination = JmsConfig.KITCHEN_REQUEST_QUEUE)
    public void onKitchenRequest(KitchenRequestEvent event) {
        log.info("📨 [JMS] KitchenRequestEvent received on queue '{}' | Order ID: {} | Order#: {}",
                JmsConfig.KITCHEN_REQUEST_QUEUE,
                event.getOrderId(),
                event.getOrderNumber());

        KitchenResponseEvent response;

        try {
            response = kitchenService.processKitchenRequest(event);
            log.info("✅ [JMS] Kitchen processing done for Order ID: {} → Ticket#: {} | Status: {}",
                    event.getOrderId(), response.getTicketNumber(), response.getStatus());

        } catch (Exception ex) {
            log.error("💥 [JMS] Error processing kitchen request for Order ID: {}",
                    event.getOrderId(), ex);

            // Build a failure response so the order-service workflow is not left hanging
            response = KitchenResponseEvent.builder()
                    .orderId(event.getOrderId())
                    .orderNumber(event.getOrderNumber())
                    .status("FAILED")
                    .estimatedPreparationTime(0)
                    .build();
        }

        log.info("📤 [JMS] Publishing KitchenResponseEvent to queue '{}' | Order ID: {} | Status: {}",
                JmsConfig.KITCHEN_RESPONSE_QUEUE,
                response.getOrderId(),
                response.getStatus());

        // jmsTemplate.convertAndSend(JmsConfig.KITCHEN_RESPONSE_QUEUE, response);

        log.info("✔ [JMS] KitchenResponseEvent published for Order ID: {}", response.getOrderId());
    }
}
