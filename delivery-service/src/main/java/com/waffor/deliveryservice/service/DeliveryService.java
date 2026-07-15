package com.waffor.deliveryservice.service;

import com.waffor.deliveryservice.dto.DeliveryResponseDto;
import com.waffor.deliveryservice.dto.event.DeliveryRequestEvent;
import com.waffor.deliveryservice.dto.event.DeliveryResponseEvent;
import com.waffor.deliveryservice.entity.Delivery;
import com.waffor.deliveryservice.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

/**
 * Core delivery service that assigns a mock driver and manages the delivery lifecycle.
 *
 * <p>Delivery lifecycle stages:
 * <ol>
 *   <li><b>ASSIGNED</b>   – a mock driver is picked and the record is created.</li>
 *   <li><b>OUT_FOR_DELIVERY</b> – driver departs with the order (simulated immediately).</li>
 *   <li><b>DELIVERED</b>  – order reaches the customer; deliveryTime is stamped.</li>
 * </ol>
 * </p>
 *
 * <p>Both JMS-initiated (from Camunda via {@code DeliveryDelegate}) and
 * REST-initiated requests use the shared {@link #assignAndDeliver} method.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryService {

    private static final Random RANDOM = new Random();

    /** Pool of mock drivers available for assignment. */
    private static final List<String> DRIVER_NAMES = List.of(
            "Ravi Kumar",
            "Priya Sharma",
            "Arun Singh",
            "Meena Nair",
            "Suresh Patel",
            "Anita Rao",
            "Vikram Joshi",
            "Deepa Menon"
    );

    private final DeliveryRepository deliveryRepository;

    // -----------------------------------------------------------------------
    // JMS path — called from DeliveryRequestConsumer
    // -----------------------------------------------------------------------

    /**
     * Process a delivery assignment request arriving via JMS.
     *
     * @param event the incoming delivery request event from the order-service
     * @return a {@link DeliveryResponseEvent} with status {@code DELIVERED}
     */
    @Transactional
    public DeliveryResponseEvent processDeliveryRequest(DeliveryRequestEvent event) {
        log.info("🚚 [JMS] Delivery request received | Order ID: {} | Order#: {}",
                event.getOrderId(), event.getOrderNumber());

        Delivery delivery = assignAndDeliver(
                event.getOrderId(),
                event.getOrderNumber(),
                event.getCustomerId()
        );

        log.info("✅ [JMS] Delivery complete | DeliveryNumber: {} | Driver: {} (ID:{}) | Status: {}",
                delivery.getDeliveryNumber(), delivery.getStatus(),
                delivery.getDriverId(), delivery.getStatus());

        return DeliveryResponseEvent.builder()
                .orderId(delivery.getOrderId())
                .orderNumber(event.getOrderNumber())
                .deliveryNumber(delivery.getDeliveryNumber())
                .driverId(delivery.getDriverId())
                .driverName(DRIVER_NAMES.get((int)(delivery.getDriverId() - 1)))
                .deliveryAddress(delivery.getDeliveryAddress())
                .status(delivery.getStatus())
                .build();
    }

    // -----------------------------------------------------------------------
    // REST path — called from DeliveryController
    // -----------------------------------------------------------------------

    /**
     * Assign a driver and deliver an order via REST request.
     *
     * @param orderId     the order ID to deliver
     * @param orderNumber the human-readable order number
     * @param customerId  the customer ID (used for address generation)
     * @return a {@link DeliveryResponseDto} describing the completed delivery
     */
    @Transactional
    public DeliveryResponseDto createDelivery(Long orderId, String orderNumber, Long customerId) {
        log.info("🌐 [REST] Delivery request | Order ID: {} | Order#: {}", orderId, orderNumber);

        Delivery delivery = assignAndDeliver(orderId, orderNumber, customerId);
        String driverName = DRIVER_NAMES.get((int)(delivery.getDriverId() - 1));

        log.info("✅ [REST] Delivery complete | DeliveryNumber: {} | Driver: {}", 
                delivery.getDeliveryNumber(), driverName);

        return toResponseDto(delivery, orderNumber, driverName);
    }

    /**
     * Retrieve a delivery record by order ID (read-only).
     *
     * @param orderId the order to look up
     * @return response DTO for the found delivery
     * @throws IllegalArgumentException if no delivery exists for the given order
     */
    @Transactional(readOnly = true)
    public DeliveryResponseDto getDeliveryByOrderId(Long orderId) {
        log.info("🔍 [REST] Fetching delivery for Order ID: {}", orderId);

        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No delivery found for Order ID: " + orderId));

        String driverName = DRIVER_NAMES.get((int)(delivery.getDriverId() - 1));

        log.info("📋 [REST] Found delivery | DeliveryNumber: {} | Status: {}",
                delivery.getDeliveryNumber(), delivery.getStatus());

        return toResponseDto(delivery, null, driverName);
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    /**
     * Core delivery logic shared by both REST and JMS paths.
     *
     * <p>Simulates the full lifecycle: ASSIGNED → OUT_FOR_DELIVERY → DELIVERED,
     * persisting the record at each stage with detailed console logging.</p>
     */
    private Delivery assignAndDeliver(Long orderId, String orderNumber, Long customerId) {
        // ---- Step 1: Pick a mock driver ----
        int driverIndex = RANDOM.nextInt(DRIVER_NAMES.size());
        long driverId   = driverIndex + 1; // IDs start at 1
        String driverName = DRIVER_NAMES.get(driverIndex);
        String deliveryAddress = "Customer #" + customerId + " Address, Waffor City";
        String deliveryNumber  = generateDeliveryNumber(orderNumber);

        log.info("👤 [Delivery] Assigned Driver: {} (ID: {}) for Order ID: {}",
                driverName, driverId, orderId);
        log.info("📍 [Delivery] Delivery address: {} | DeliveryNumber: {}",
                deliveryAddress, deliveryNumber);

        // ---- Stage 1: ASSIGNED ----
        Delivery delivery = Delivery.builder()
                .orderId(orderId)
                .deliveryNumber(deliveryNumber)
                .driverId(driverId)
                .deliveryAddress(deliveryAddress)
                .status("ASSIGNED")
                .build();
        delivery = deliveryRepository.save(delivery);
        log.info("💾 [Delivery] Record saved with status ASSIGNED (DB id: {})", delivery.getId());

        // ---- Stage 2: OUT_FOR_DELIVERY ----
        log.info("🛵 [Delivery] Driver {} is OUT_FOR_DELIVERY for Order ID: {}", driverName, orderId);
        delivery.setStatus("OUT_FOR_DELIVERY");
        delivery = deliveryRepository.save(delivery);

        // Simulate delivery dispatch time
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Stage 3: DELIVERED ----
        log.info("🏠 [Delivery] Order ID: {} has been DELIVERED to: {}", orderId, deliveryAddress);
        delivery.setStatus("DELIVERED");
        delivery.setDeliveryTime(LocalDateTime.now());
        delivery = deliveryRepository.save(delivery);
        log.info("[DeliveryService] DELIVERED");


        return delivery;
    }

    /**
     * Generates a unique delivery number: {@code DLV-{orderNumber}-{timestamp}}.
     */
    private String generateDeliveryNumber(String orderNumber) {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        return "DLV-" + orderNumber + "-" + timestamp;
    }

    /**
     * Maps a {@link Delivery} entity to a {@link DeliveryResponseDto}.
     */
    private DeliveryResponseDto toResponseDto(Delivery delivery, String orderNumber, String driverName) {
        return DeliveryResponseDto.builder()
                .id(delivery.getId())
                .orderId(delivery.getOrderId())
                .orderNumber(orderNumber)
                .deliveryNumber(delivery.getDeliveryNumber())
                .driverId(delivery.getDriverId())
                .driverName(driverName)
                .deliveryAddress(delivery.getDeliveryAddress())
                .status(delivery.getStatus())
                .deliveryTime(delivery.getDeliveryTime())
                .createdAt(delivery.getCreatedAt())
                .build();
    }
}
