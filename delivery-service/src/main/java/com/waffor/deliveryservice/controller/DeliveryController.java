package com.waffor.deliveryservice.controller;

import com.waffor.deliveryservice.dto.DeliveryResponseDto;
import com.waffor.deliveryservice.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller exposing delivery management endpoints.
 *
 * <p>Base path: {@code /api/delivery}</p>
 *
 * <p>Endpoints:
 * <ul>
 *   <li>{@code POST /api/delivery}               – Assign driver and deliver an order</li>
 *   <li>{@code GET  /api/delivery/{orderId}}      – Get delivery status for an order</li>
 *   <li>{@code GET  /api/delivery/health}         – Health check</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/api/delivery")
@RequiredArgsConstructor
@Slf4j
public class DeliveryController {

    private final DeliveryService deliveryService;

    // -----------------------------------------------------------------------
    // POST /api/delivery
    // -----------------------------------------------------------------------

    /**
     * Assign a mock driver and simulate delivery for an order.
     *
     * <p>Runs the full ASSIGNED → OUT_FOR_DELIVERY → DELIVERED lifecycle
     * and returns the final DELIVERED record.</p>
     *
     * @param orderId     order to deliver (query param)
     * @param orderNumber human-readable order reference (query param)
     * @param customerId  customer ID used for address generation (query param)
     * @return 201 Created with the delivery details
     */
    @PostMapping
    public ResponseEntity<DeliveryResponseDto> createDelivery(
            @RequestParam("orderId") Long   orderId,
            @RequestParam("orderNumber") String orderNumber,
            @RequestParam("customerId") Long   customerId) {

        log.info("🌐 [REST] POST /api/delivery | Order ID: {} | Order#: {} | Customer ID: {}",
                orderId, orderNumber, customerId);

        DeliveryResponseDto response = deliveryService.createDelivery(orderId, orderNumber, customerId);

        log.info("🌐 [REST] Delivery created | DeliveryNumber: {} | Driver: {} | Status: {}",
                response.getDeliveryNumber(), response.getDriverName(), response.getStatus());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // -----------------------------------------------------------------------
    // GET /api/delivery/{orderId}
    // -----------------------------------------------------------------------

    /**
     * Retrieve the delivery record for a specific order.
     *
     * @param orderId the order to look up
     * @return 200 OK with delivery details, or 404 if not found
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<DeliveryResponseDto> getDelivery(@PathVariable("orderId") Long orderId) {
        log.info("🌐 [REST] GET /api/delivery/{}", orderId);

        DeliveryResponseDto response = deliveryService.getDeliveryByOrderId(orderId);

        log.info("🌐 [REST] Delivery found for Order ID: {} | Status: {}",
                orderId, response.getStatus());

        return ResponseEntity.ok(response);
    }

    // -----------------------------------------------------------------------
    // GET /api/delivery/health
    // -----------------------------------------------------------------------

    /**
     * Simple health check.
     *
     * @return 200 OK with service status details
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        log.debug("🌐 [REST] GET /api/delivery/health");
        return ResponseEntity.ok(Map.of(
                "service", "delivery-service",
                "status",  "UP",
                "port",    "8084"
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("⚠️ [REST] IllegalArgumentException handled: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }
}
