package com.waffor.orderservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

@Configuration
@EnableJms
public class JmsConfig {

    /** Queue on which the order-service publishes new orders (consumed by order-service itself via Camunda). */
    public static final String ORDER_CREATED_QUEUE = "order.created";

    /** Queue on which the order-service publishes payment requests (consumed by payment-service). */
    public static final String PAYMENT_REQUEST_QUEUE = "payment.request";

    /** Queue on which the payment-service publishes results (consumed by order-service PaymentDelegate). */
    public static final String PAYMENT_RESPONSE_QUEUE = "payment.response";

    /** Queue on which the order-service publishes kitchen work requests (consumed by kitchen-service). */
    public static final String KITCHEN_REQUEST_QUEUE = "kitchen.request";

    /** Queue on which the kitchen-service publishes READY responses (consumed by order-service KitchenDelegate). */
    public static final String KITCHEN_RESPONSE_QUEUE = "kitchen.response";

    /** Queue on which the order-service publishes delivery assignment requests (consumed by delivery-service). */
    public static final String DELIVERY_REQUEST_QUEUE = "delivery.request";

    /** Queue on which the delivery-service publishes DELIVERED responses (consumed by order-service DeliveryDelegate). */
    public static final String DELIVERY_RESPONSE_QUEUE = "delivery.response";



    @Bean
    public MessageConverter messageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");

        java.util.Map<String, Class<?>> typeIdMappings = new java.util.HashMap<>();
        typeIdMappings.put("OrderCreatedEvent", com.waffor.orderservice.dto.event.OrderCreatedEvent.class);
        typeIdMappings.put("PaymentRequestEvent", com.waffor.orderservice.dto.event.PaymentRequestEvent.class);
        typeIdMappings.put("PaymentResponseEvent", com.waffor.orderservice.dto.event.PaymentResponseEvent.class);
        typeIdMappings.put("KitchenRequestEvent", com.waffor.orderservice.dto.event.KitchenRequestEvent.class);
        typeIdMappings.put("KitchenResponseEvent", com.waffor.orderservice.dto.event.KitchenResponseEvent.class);
        typeIdMappings.put("DeliveryRequestEvent", com.waffor.orderservice.dto.event.DeliveryRequestEvent.class);
        typeIdMappings.put("DeliveryResponseEvent", com.waffor.orderservice.dto.event.DeliveryResponseEvent.class);
        converter.setTypeIdMappings(typeIdMappings);

        return converter;
    }

}

