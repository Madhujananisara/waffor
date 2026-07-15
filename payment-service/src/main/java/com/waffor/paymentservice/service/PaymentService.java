package com.waffor.paymentservice.service;

import com.waffor.paymentservice.dto.PaymentRequestDto;
import com.waffor.paymentservice.dto.PaymentResponseDto;
import com.waffor.paymentservice.dto.event.PaymentRequestEvent;
import com.waffor.paymentservice.dto.event.PaymentResponseEvent;
import com.waffor.paymentservice.entity.Payment;
import com.waffor.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.UUID;

/**
 * Core payment processing service.
 *
 * <p>Handles both REST-initiated and JMS-initiated payment requests.
 * Uses a mock payment gateway that randomly succeeds or fails (70% success rate)
 * to simulate real-world payment scenarios.</p>
 *
 * <p>Every payment attempt — whether successful or failed — is persisted
 * to the {@code payments} table for auditing.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private static final double SUCCESS_RATE = 1.0; // 100% chance of success for demo
    private static final Random RANDOM = new Random();

    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_FAILED  = "FAILED";

    private final PaymentRepository paymentRepository;

    // -----------------------------------------------------------------------
    // Public API – called from PaymentController (REST)
    // -----------------------------------------------------------------------

    /**
     * Process a payment from a REST request.
     *
     * @param request DTO containing order details and amount
     * @return PaymentResponseDto with the result of the payment attempt
     */
    @Transactional
    public PaymentResponseDto processPayment(PaymentRequestDto request) {
        log.info("[REST] Starting payment processing for Order ID: {} | Order#: {} | Amount: {}",
                request.getOrderId(), request.getOrderNumber(), request.getAmount());

        String paymentMethod = request.getPaymentMethod() != null
                ? request.getPaymentMethod()
                : "CREDIT_CARD";

        Payment payment = executePayment(
                request.getOrderId(),
                request.getOrderNumber(),
                request.getAmount(),
                paymentMethod
        );

        log.info("[REST] Payment processing complete for Order ID: {} → Status: {}",
                request.getOrderId(), payment.getStatus());

        return toResponseDto(payment, null);
    }

    // -----------------------------------------------------------------------
    // Internal API – called from PaymentRequestConsumer (JMS)
    // -----------------------------------------------------------------------

    /**
     * Process a payment from a JMS event (sent by order-service PaymentDelegate).
     *
     * @param event the incoming payment request event
     * @return PaymentResponseEvent to be sent back on the response queue
     */
    @Transactional
    public PaymentResponseEvent processPaymentEvent(PaymentRequestEvent event) {
        log.info("[JMS] Starting payment processing for Order ID: {} | Order#: {} | Amount: {}",
                event.getOrderId(), event.getOrderNumber(), event.getTotalAmount());

        Payment payment = executePayment(
                event.getOrderId(),
                event.getOrderNumber(),
                event.getTotalAmount(),
                "ONLINE_GATEWAY"
        );

        boolean isSuccess = STATUS_SUCCESS.equals(payment.getStatus());
        String failureReason = isSuccess ? null : "Mock gateway declined the transaction";

        log.info("[JMS] Payment processing complete for Order ID: {} → Status: {}",
                event.getOrderId(), payment.getStatus());

        return PaymentResponseEvent.builder()
                .orderId(payment.getOrderId())
                .orderNumber(event.getOrderNumber())
                .paymentNumber(payment.getPaymentNumber())
                .transactionId(payment.getTransactionId())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .failureReason(failureReason)
                .build();
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    /**
     * Core payment execution logic shared by both REST and JMS paths.
     * Simulates a payment gateway call, persists the record, and returns it.
     */
    private Payment executePayment(Long orderId, String orderNumber,
                                   BigDecimal amount, String paymentMethod) {

        String paymentNumber = generatePaymentNumber(orderNumber);
        log.debug("Generated payment number: {} for Order ID: {}", paymentNumber, orderId);

        // ---- Mock gateway call ----
        boolean gatewaySuccess = simulateGatewayCall(orderId, amount);

        String status;
        String transactionId;

        if (gatewaySuccess) {
            status        = STATUS_SUCCESS;
            transactionId = "TXN-" + UUID.randomUUID().toString().toUpperCase();
            log.info("[PaymentService] Payment SUCCESS");

        } else {
            status        = STATUS_FAILED;
            transactionId = null; // No transaction ID on failure
            log.warn("❌ Mock gateway DECLINED payment for Order ID: {} | Amount: {}", orderId, amount);
        }

        // ---- Persist record ----
        Payment payment = Payment.builder()
                .orderId(orderId)
                .paymentNumber(paymentNumber)
                .amount(amount)
                .status(status)
                .paymentMethod(paymentMethod)
                .transactionId(transactionId)
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        log.info("💾 Payment record saved with ID: {} for Order ID: {}", savedPayment.getId(), orderId);

        return savedPayment;
    }

    /**
     * Simulates a mock payment gateway call with a random success/failure outcome.
     * 70% success rate by default.
     *
     * @param orderId used only for logging
     * @param amount  used for logging; could be used for threshold-based rules
     * @return true if gateway approved, false if declined
     */
    private boolean simulateGatewayCall(Long orderId, BigDecimal amount) {
        log.debug("Calling mock payment gateway for Order ID: {} | Amount: {}", orderId, amount);

        // Simulate network latency (2 seconds)
        try {
            long delay = 2000L;
            log.debug("Mock gateway response delay: {}ms", delay);
            Thread.sleep(delay);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            log.warn("Gateway simulation interrupted for Order ID: {}", orderId);
        }

        double roll = RANDOM.nextDouble();
        boolean approved = roll < SUCCESS_RATE;
        log.debug("Gateway roll: {:.4f} | Threshold: {} | Approved: {}", roll, SUCCESS_RATE, approved);
        return approved;
    }

    /**
     * Generates a unique payment number of the format PAY-{orderNumber}-{timestamp}.
     */
    private String generatePaymentNumber(String orderNumber) {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        return "PAY-" + orderNumber + "-" + timestamp;
    }

    /**
     * Maps a persisted {@link Payment} entity to a {@link PaymentResponseDto}.
     */
    private PaymentResponseDto toResponseDto(Payment payment, String failureReason) {
        return PaymentResponseDto.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .paymentNumber(payment.getPaymentNumber())
                .transactionId(payment.getTransactionId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .failureReason(STATUS_FAILED.equals(payment.getStatus())
                        ? "Mock gateway declined the transaction"
                        : null)
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
