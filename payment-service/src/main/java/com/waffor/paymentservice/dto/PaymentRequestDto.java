package com.waffor.paymentservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

/**
 * REST request body for the POST /api/payments endpoint.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequestDto {

    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotNull(message = "Order number is required")
    @Size(min = 1, max = 50, message = "Order number must be between 1 and 50 characters")
    private String orderNumber;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    /** Payment method, e.g. CREDIT_CARD, DEBIT_CARD, UPI, WALLET */
    @Size(max = 30, message = "Payment method must not exceed 30 characters")
    private String paymentMethod;
}
