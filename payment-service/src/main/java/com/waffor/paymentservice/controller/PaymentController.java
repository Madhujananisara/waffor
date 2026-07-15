package com.waffor.paymentservice.controller;

import com.waffor.paymentservice.dto.PaymentRequestDto;
import com.waffor.paymentservice.dto.PaymentResponseDto;
import com.waffor.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller exposing payment processing endpoints.
 *
 * <p>Base path: {@code /api/payments}</p>
 *
 * <p>Endpoints:
 * <ul>
 *   <li>{@code POST /api/payments} – Initiate a payment (mock processing)</li>
 *   <li>{@code GET  /api/payments/health} – Health check endpoint</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    // -----------------------------------------------------------------------
    // POST /api/payments
    // -----------------------------------------------------------------------

    /**
     * Initiate payment processing for an order.
     *
     * <p>Performs a mock payment via a simulated gateway with a 70% success rate.
     * The payment record is persisted regardless of outcome.</p>
     *
     * @param request payment details including orderId, amount, and method
     * @return 201 Created with payment result on success/failure (both are valid outcomes)
     */
    @PostMapping
    public ResponseEntity<PaymentResponseDto> processPayment(
            @Valid @RequestBody PaymentRequestDto request) {

        log.info("🌐 [REST] POST /api/payments → Order ID: {} | Amount: {} | Method: {}",
                request.getOrderId(), request.getAmount(), request.getPaymentMethod());

        PaymentResponseDto response = paymentService.processPayment(request);

        log.info("🌐 [REST] Payment response for Order ID: {} → Status: {} | PaymentNumber: {}",
                request.getOrderId(), response.getStatus(), response.getPaymentNumber());

        // Return 201 for both SUCCESS and FAILED – the payment record was created either way
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // -----------------------------------------------------------------------
    // GET /api/payments/health
    // -----------------------------------------------------------------------

    /**
     * Simple health check to verify the service is running.
     *
     * @return 200 OK with status message
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        log.debug("🌐 [REST] GET /api/payments/health");
        return ResponseEntity.ok(Map.of(
                "service", "payment-service",
                "status",  "UP",
                "port",    "8082"
        ));
    }
}
