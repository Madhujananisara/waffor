package com.waffor.paymentservice.dto.event;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequestEvent {
    private Long orderId;
    private String orderNumber;
    private BigDecimal totalAmount;
}
