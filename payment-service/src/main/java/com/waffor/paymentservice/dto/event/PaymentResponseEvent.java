package com.waffor.paymentservice.dto.event;

import lombok.*;

import java.math.BigDecimal;

/**
 * Event sent back to the order-service on the payment.response queue
 * after payment processing is complete.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponseEvent {

    /** The order ID this payment is for */
    private Long orderId;

    /** The order number this payment is for */
    private String orderNumber;

    /** The generated payment reference number */
    private String paymentNumber;

    /** The unique transaction ID from the mock payment gateway */
    private String transactionId;

    /** Amount that was charged */
    private BigDecimal amount;

    /** Payment status: SUCCESS or FAILED */
    private String status;

    /** Human-readable reason for failure (null on success) */
    private String failureReason;
}
