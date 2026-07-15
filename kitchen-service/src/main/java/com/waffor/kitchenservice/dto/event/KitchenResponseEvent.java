package com.waffor.kitchenservice.dto.event;

import lombok.*;

/**
 * Event sent back to the order-service on the {@code kitchen.response} queue
 * once the kitchen ticket has been created and marked READY.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KitchenResponseEvent {

    /** ID of the order this response is for. */
    private Long orderId;

    /** Order number for correlation. */
    private String orderNumber;

    /** The generated kitchen ticket number. */
    private String ticketNumber;

    /** Current ticket status — always {@code READY} on success. */
    private String status;

    /** Estimated preparation time in minutes. */
    private Integer estimatedPreparationTime;
}
