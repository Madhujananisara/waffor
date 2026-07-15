package com.waffor.deliveryservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

/**
 * JMS configuration for the delivery-service.
 *
 * <p>Queue names mirror those used in the order-service JmsConfig
 * to ensure both sides agree on the same destination strings.</p>
 */
@Configuration
@EnableJms
public class JmsConfig {

    /** Queue on which the order-service publishes delivery assignment requests. */
    public static final String DELIVERY_REQUEST_QUEUE = "delivery.request";

    /** Queue on which the delivery-service publishes DELIVERED responses. */
    public static final String DELIVERY_RESPONSE_QUEUE = "delivery.response";

    @Bean
    public MessageConverter messageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");

        java.util.Map<String, Class<?>> typeIdMappings = new java.util.HashMap<>();
        typeIdMappings.put("DeliveryRequestEvent", com.waffor.deliveryservice.dto.event.DeliveryRequestEvent.class);
        typeIdMappings.put("DeliveryResponseEvent", com.waffor.deliveryservice.dto.event.DeliveryResponseEvent.class);
        converter.setTypeIdMappings(typeIdMappings);

        return converter;
    }

}
