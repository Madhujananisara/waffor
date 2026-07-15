package com.waffor.deliveryservice.dto.event;

import lombok.*;

/**
 * Event sent back to the order-service on the {@code delivery.response} queue
 * once the delivery has been assigned and completed (DELIVERED status).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryResponseEvent {

    /** ID of the order that was delivered. */
    private Long orderId;

    /** Order number for correlation. */
    private String orderNumber;

    /** The generated delivery reference number. */
    private String deliveryNumber;

    /** ID of the mock driver assigned. */
    private Long driverId;

    /** Name of the mock driver assigned. */
    private String driverName;

    /** Delivery address used. */
    private String deliveryAddress;

    /** Delivery status — {@code DELIVERED} on success. */
    private String status;
}
