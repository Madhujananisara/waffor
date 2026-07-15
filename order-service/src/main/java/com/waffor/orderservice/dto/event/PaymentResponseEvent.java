package com.waffor.orderservice.dto.event;

import lombok.*;

import java.math.BigDecimal;

/**
 * Event consumed by the order-service {@code PaymentDelegate}
 * from the {@code payment.response} queue after payment-service has processed the payment.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponseEvent {

    /** The order ID this payment was for */
    private Long orderId;

    /** The order number this payment was for */
    private String orderNumber;

    /** Generated payment reference number */
    private String paymentNumber;

    /** Unique transaction ID from the mock gateway (null on failure) */
    private String transactionId;

    /** Amount that was charged */
    private BigDecimal amount;

    /** SUCCESS or FAILED */
    private String status;

    /** Human-readable failure reason (null on success) */
    private String failureReason;
}
