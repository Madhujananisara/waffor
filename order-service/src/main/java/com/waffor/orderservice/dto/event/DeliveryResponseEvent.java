package com.waffor.orderservice.dto.event;

import lombok.*;

/**
 * Event received by the order-service {@code DeliveryDelegate}
 * from the {@code delivery.response} queue after the delivery-service
 * has completed delivery.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryResponseEvent {

    /** The order ID that was delivered. */
    private Long orderId;

    /** The order number for correlation. */
    private String orderNumber;

    /** The generated delivery reference number. */
    private String deliveryNumber;

    /** ID of the mock driver who completed the delivery. */
    private Long driverId;

    /** Name of the mock driver who completed the delivery. */
    private String driverName;

    /** Delivery address used. */
    private String deliveryAddress;

    /** Delivery status — {@code DELIVERED} on success, {@code FAILED} on error. */
    private String status;
}
