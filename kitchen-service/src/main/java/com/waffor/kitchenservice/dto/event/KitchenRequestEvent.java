package com.waffor.kitchenservice.dto.event;

import lombok.*;

/**
 * Event received from the order-service on the {@code kitchen.request} queue.
 * Contains the minimal order details needed to create a kitchen ticket.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KitchenRequestEvent {

    /** ID of the order that was paid and needs preparation. */
    private Long orderId;

    /** Human-readable order reference number. */
    private String orderNumber;

    /** ID of the customer who placed the order. */
    private Long customerId;
}
