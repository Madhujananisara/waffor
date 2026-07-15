package com.waffor.kitchenservice.controller;

import com.waffor.kitchenservice.dto.KitchenTicketResponseDto;
import com.waffor.kitchenservice.service.KitchenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller exposing kitchen ticket management endpoints.
 *
 * <p>Base path: {@code /api/kitchen}</p>
 *
 * <p>Endpoints:
 * <ul>
 *   <li>{@code POST /api/kitchen/tickets}           – Create a kitchen ticket for an order</li>
 *   <li>{@code GET  /api/kitchen/tickets/{orderId}} – Get ticket status for an order</li>
 *   <li>{@code GET  /api/kitchen/health}            – Health check</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/api/kitchen")
@RequiredArgsConstructor
@Slf4j
public class KitchenController {

    private final KitchenService kitchenService;

    // -----------------------------------------------------------------------
    // POST /api/kitchen/tickets
    // -----------------------------------------------------------------------

    /**
     * Create a kitchen ticket for a paid order.
     *
     * <p>Simulates the full RECEIVED → PREPARING → READY lifecycle and returns
     * the ticket in {@code READY} status.</p>
     *
     * @param orderId     path to the order (query param)
     * @param orderNumber human-readable order number (query param)
     * @return 201 Created with the kitchen ticket details
     */
    @PostMapping("/tickets")
    public ResponseEntity<KitchenTicketResponseDto> createTicket(
            @RequestParam("orderId") Long orderId,
            @RequestParam("orderNumber") String orderNumber) {

        log.info("🌐 [REST] POST /api/kitchen/tickets | Order ID: {} | Order#: {}",
                orderId, orderNumber);

        KitchenTicketResponseDto response = kitchenService.createKitchenTicket(orderId, orderNumber);

        log.info("🌐 [REST] Ticket created | Ticket#: {} | Status: {}",
                response.getTicketNumber(), response.getStatus());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // -----------------------------------------------------------------------
    // GET /api/kitchen/tickets/{orderId}
    // -----------------------------------------------------------------------

    /**
     * Retrieve the kitchen ticket for a specific order.
     *
     * @param orderId the order to look up
     * @return 200 OK with ticket details, or 404 if not found
     */
    @GetMapping("/tickets/{orderId}")
    public ResponseEntity<KitchenTicketResponseDto> getTicketByOrderId(
            @PathVariable("orderId") Long orderId) {

        log.info("🌐 [REST] GET /api/kitchen/tickets/{}", orderId);

        KitchenTicketResponseDto response = kitchenService.getTicketByOrderId(orderId);

        log.info("🌐 [REST] Ticket found for Order ID: {} | Status: {}",
                orderId, response.getStatus());

        return ResponseEntity.ok(response);
    }

    // -----------------------------------------------------------------------
    // GET /api/kitchen/health
    // -----------------------------------------------------------------------

    /**
     * Simple health check to verify the service is running.
     *
     * @return 200 OK with status details
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        log.debug("🌐 [REST] GET /api/kitchen/health");
        return ResponseEntity.ok(Map.of(
                "service", "kitchen-service",
                "status",  "UP",
                "port",    "8083"
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("⚠️ [REST] IllegalArgumentException handled: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }
}
