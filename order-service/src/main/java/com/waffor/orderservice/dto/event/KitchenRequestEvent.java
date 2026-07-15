package com.waffor.orderservice.dto.event;

import lombok.*;

/**
 * Event published by the order-service {@code KitchenDelegate}
 * to the {@code kitchen.request} queue, consumed by the kitchen-service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KitchenRequestEvent {

    /** ID of the order that has been paid and needs kitchen preparation. */
    private Long orderId;

    /** Human-readable order reference number. */
    private String orderNumber;

    /** Customer who placed the order. */
    private Long customerId;
}
