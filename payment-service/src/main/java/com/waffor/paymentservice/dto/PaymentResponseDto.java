package com.waffor.paymentservice.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * REST response body returned by the POST /api/payments endpoint.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponseDto {

    private Long id;
    private Long orderId;
    private String orderNumber;
    private String paymentNumber;
    private String transactionId;
    private BigDecimal amount;
    private String paymentMethod;

    /** SUCCESS or FAILED */
    private String status;

    /** Present only when status is FAILED */
    private String failureReason;

    private LocalDateTime createdAt;
}
