package com.waffor.orderservice.dto.event;

import lombok.*;

/**
 * Event published by the order-service {@code DeliveryDelegate}
 * to the {@code delivery.request} queue, consumed by the delivery-service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryRequestEvent {

    /** ID of the order ready for delivery (kitchen marked it READY). */
    private Long orderId;

    /** Human-readable order reference number. */
    private String orderNumber;

    /** Customer ID — used by delivery-service to generate the mock address. */
    private Long customerId;
}
