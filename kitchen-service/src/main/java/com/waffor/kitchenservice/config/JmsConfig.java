package com.waffor.kitchenservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

/**
 * JMS configuration for the kitchen-service.
 *
 * <p>Queue names mirror those declared in the order-service JmsConfig to
 * ensure both sides agree on the same destination strings.</p>
 */
@Configuration
@EnableJms
public class JmsConfig {

    /** Queue on which the order-service publishes kitchen work requests. */
    public static final String KITCHEN_REQUEST_QUEUE = "kitchen.request";

    /** Queue on which the kitchen-service publishes READY responses. */
    public static final String KITCHEN_RESPONSE_QUEUE = "kitchen.response";

    @Bean
    public MessageConverter messageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");

        java.util.Map<String, Class<?>> typeIdMappings = new java.util.HashMap<>();
        typeIdMappings.put("KitchenRequestEvent", com.waffor.kitchenservice.dto.event.KitchenRequestEvent.class);
        typeIdMappings.put("KitchenResponseEvent", com.waffor.kitchenservice.dto.event.KitchenResponseEvent.class);
        converter.setTypeIdMappings(typeIdMappings);

        return converter;
    }

}
