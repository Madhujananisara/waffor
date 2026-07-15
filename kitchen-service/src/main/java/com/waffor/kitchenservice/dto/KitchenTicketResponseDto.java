package com.waffor.kitchenservice.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * REST response body returned by the kitchen ticket endpoints.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KitchenTicketResponseDto {

    private Long id;
    private Long orderId;
    private String orderNumber;
    private String ticketNumber;

    /** Ticket status: RECEIVED, PREPARING, READY */
    private String status;

    /** Estimated preparation time in minutes */
    private Integer estimatedPreparationTime;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
