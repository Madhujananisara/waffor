package com.waffor.orderservice.dto.event;

import lombok.*;

import java.math.BigDecimal;

/**
 * Event published by the order-service {@code PaymentDelegate}
 * to the {@code payment.request} queue, consumed by the payment-service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequestEvent {

    /** ID of the order requiring payment */
    private Long orderId;

    /** Human-readable order reference number */
    private String orderNumber;

    /** Amount to charge */
    private BigDecimal totalAmount;
}
