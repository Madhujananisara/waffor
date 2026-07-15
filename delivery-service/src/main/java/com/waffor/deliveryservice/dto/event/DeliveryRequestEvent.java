package com.waffor.deliveryservice.dto.event;

import lombok.*;

/**
 * Event received from the order-service on the {@code delivery.request} queue.
 * Contains the order details needed to assign a driver and create a delivery record.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryRequestEvent {

    /** ID of the order ready for delivery. */
    private Long orderId;

    /** Human-readable order reference number. */
    private String orderNumber;

    /** ID of the customer (used as delivery address seed). */
    private Long customerId;
}
