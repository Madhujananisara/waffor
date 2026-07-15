package com.waffor.paymentservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

@Configuration
@EnableJms
public class JmsConfig {

    public static final String PAYMENT_REQUEST_QUEUE = "payment.request";
    public static final String PAYMENT_RESPONSE_QUEUE = "payment.response";

    @Bean
    public MessageConverter messageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");

        java.util.Map<String, Class<?>> typeIdMappings = new java.util.HashMap<>();
        typeIdMappings.put("PaymentRequestEvent", com.waffor.paymentservice.dto.event.PaymentRequestEvent.class);
        typeIdMappings.put("PaymentResponseEvent", com.waffor.paymentservice.dto.event.PaymentResponseEvent.class);
        converter.setTypeIdMappings(typeIdMappings);

        return converter;
    }

}
