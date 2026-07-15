package com.waffor.kitchenservice.service;

import com.waffor.kitchenservice.dto.KitchenTicketResponseDto;
import com.waffor.kitchenservice.dto.event.KitchenRequestEvent;
import com.waffor.kitchenservice.dto.event.KitchenResponseEvent;
import com.waffor.kitchenservice.entity.KitchenTicket;
import com.waffor.kitchenservice.repository.KitchenTicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Core kitchen service responsible for creating and managing kitchen tickets.
 *
 * <p>Lifecycle of a kitchen ticket:
 * <ol>
 *   <li><b>RECEIVED</b> – ticket created the moment the request arrives.</li>
 *   <li><b>PREPARING</b> – kitchen staff acknowledged (simulated immediately).</li>
 *   <li><b>READY</b> – preparation complete; response sent back to order-service.</li>
 * </ol>
 * </p>
 *
 * <p>Both JMS-initiated (from the Camunda workflow via {@code KitchenDelegate})
 * and REST-initiated requests use the same core {@link #createTicket} method.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KitchenService {

    /** Default estimated preparation time in minutes. */
    private static final int DEFAULT_PREP_TIME_MINUTES = 15;

    private final KitchenTicketRepository kitchenTicketRepository;

    // -----------------------------------------------------------------------
    // JMS path — called from KitchenRequestConsumer
    // -----------------------------------------------------------------------

    /**
     * Process a kitchen request arriving via JMS from the order-service.
     *
     * @param event the incoming kitchen request event
     * @return a {@link KitchenResponseEvent} with status {@code READY}
     */
    @Transactional
    public KitchenResponseEvent processKitchenRequest(KitchenRequestEvent event) {
        log.info("🍳 [JMS] Kitchen request received | Order ID: {} | Order#: {}",
                event.getOrderId(), event.getOrderNumber());

        KitchenTicket ticket = createTicket(event.getOrderId(), event.getOrderNumber());

        log.info("✅ [JMS] Kitchen ticket created and READY | Ticket#: {} | Order ID: {}",
                ticket.getTicketNumber(), event.getOrderId());

        return KitchenResponseEvent.builder()
                .orderId(ticket.getOrderId())
                .orderNumber(event.getOrderNumber())
                .ticketNumber(ticket.getTicketNumber())
                .status(ticket.getStatus())
                .estimatedPreparationTime(ticket.getEstimatedPreparationTime())
                .build();
    }

    // -----------------------------------------------------------------------
    // REST path — called from KitchenController
    // -----------------------------------------------------------------------

    /**
     * Create a kitchen ticket via REST request.
     *
     * @param orderId     the order requiring preparation
     * @param orderNumber the human-readable order reference
     * @return a {@link KitchenTicketResponseDto} describing the created ticket
     */
    @Transactional
    public KitchenTicketResponseDto createKitchenTicket(Long orderId, String orderNumber) {
        log.info("🌐 [REST] Kitchen ticket request | Order ID: {} | Order#: {}", orderId, orderNumber);

        KitchenTicket ticket = createTicket(orderId, orderNumber);

        log.info("✅ [REST] Kitchen ticket created | Ticket#: {} | Status: {}",
                ticket.getTicketNumber(), ticket.getStatus());

        return toResponseDto(ticket, orderNumber);
    }

    /**
     * Retrieve a ticket by its order ID (read-only).
     *
     * @param orderId the order to look up
     * @return response DTO for the found ticket
     * @throws IllegalArgumentException if no ticket exists for the given order
     */
    @Transactional(readOnly = true)
    public KitchenTicketResponseDto getTicketByOrderId(Long orderId) {
        log.info("🔍 [REST] Fetching kitchen ticket for Order ID: {}", orderId);

        KitchenTicket ticket = kitchenTicketRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No kitchen ticket found for Order ID: " + orderId));

        log.info("📋 [REST] Found kitchen ticket | Ticket#: {} | Status: {}",
                ticket.getTicketNumber(), ticket.getStatus());

        return toResponseDto(ticket, null);
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    /**
     * Core ticket creation logic shared by both REST and JMS paths.
     *
     * <p>Simulates the three-stage kitchen lifecycle (RECEIVED → PREPARING → READY)
     * synchronously, persisting the final READY state.</p>
     */
    private KitchenTicket createTicket(Long orderId, String orderNumber) {
        String ticketNumber = generateTicketNumber(orderNumber);

        // Stage 1 – RECEIVED
        log.info("📥 [Kitchen] Ticket {} RECEIVED for Order ID: {}", ticketNumber, orderId);
        KitchenTicket ticket = KitchenTicket.builder()
                .orderId(orderId)
                .ticketNumber(ticketNumber)
                .status("RECEIVED")
                .estimatedPreparationTime(DEFAULT_PREP_TIME_MINUTES)
                .build();
        ticket = kitchenTicketRepository.save(ticket);
        log.info("💾 [Kitchen] Ticket {} saved with status RECEIVED (DB id: {})",
                ticketNumber, ticket.getId());

        // Stage 2 – PREPARING
        log.info("👨‍🍳 [Kitchen] Ticket {} → PREPARING | Est. time: {} min",
                ticketNumber, DEFAULT_PREP_TIME_MINUTES);
        ticket.setStatus("PREPARING");
        ticket = kitchenTicketRepository.save(ticket);

        // Simulate preparation time
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Stage 3 – READY (preparation complete)
        log.info("🔔 [Kitchen] Ticket {} → READY | Order ID: {} is prepared!", ticketNumber, orderId);
        ticket.setStatus("READY");
        ticket = kitchenTicketRepository.save(ticket);
        log.info("[KitchenService] READY");


        return ticket;
    }

    /**
     * Generates a unique ticket number: {@code KT-{orderNumber}-{timestamp}}.
     */
    private String generateTicketNumber(String orderNumber) {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        return "KT-" + orderNumber + "-" + timestamp;
    }

    /**
     * Maps a {@link KitchenTicket} entity to a {@link KitchenTicketResponseDto}.
     */
    private KitchenTicketResponseDto toResponseDto(KitchenTicket ticket, String orderNumber) {
        return KitchenTicketResponseDto.builder()
                .id(ticket.getId())
                .orderId(ticket.getOrderId())
                .orderNumber(orderNumber)
                .ticketNumber(ticket.getTicketNumber())
                .status(ticket.getStatus())
                .estimatedPreparationTime(ticket.getEstimatedPreparationTime())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .build();
    }
}
