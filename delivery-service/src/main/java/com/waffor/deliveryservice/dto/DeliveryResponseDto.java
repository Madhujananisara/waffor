package com.waffor.deliveryservice.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * REST response body for delivery endpoints.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryResponseDto {

    private Long id;
    private Long orderId;
    private String orderNumber;
    private String deliveryNumber;

    /** Mock driver ID assigned to this delivery */
    private Long driverId;

    /** Mock driver name assigned to this delivery */
    private String driverName;

    private String deliveryAddress;

    /** ASSIGNED, OUT_FOR_DELIVERY, DELIVERED */
    private String status;

    /** Timestamp when delivery was completed */
    private LocalDateTime deliveryTime;

    private LocalDateTime createdAt;
}
