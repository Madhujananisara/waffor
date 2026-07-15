package com.waffor.orderservice.dto.event;

import lombok.*;

/**
 * Event received by the order-service {@code KitchenDelegate}
 * from the {@code kitchen.response} queue after the kitchen-service
 * has created and marked the ticket as READY.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KitchenResponseEvent {

    /** The order ID this kitchen ticket is for. */
    private Long orderId;

    /** The order number for correlation. */
    private String orderNumber;

    /** The generated kitchen ticket number. */
    private String ticketNumber;

    /** Ticket status — {@code READY} on success, {@code FAILED} on error. */
    private String status;

    /** Estimated preparation time in minutes. */
    private Integer estimatedPreparationTime;
}
