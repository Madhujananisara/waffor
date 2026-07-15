package com.waffor.orderservice.service.impl;

import com.waffor.orderservice.dto.OrderItemResponseDto;
import com.waffor.orderservice.dto.OrderRequestDto;
import com.waffor.orderservice.dto.OrderResponseDto;
import com.waffor.orderservice.dto.event.OrderCreatedEvent;
import com.waffor.orderservice.entity.Order;
import com.waffor.orderservice.entity.OrderItem;
import com.waffor.orderservice.exception.ResourceNotFoundException;
import com.waffor.orderservice.messaging.OrderEventProducer;
import com.waffor.orderservice.repository.OrderRepository;
import com.waffor.orderservice.repository.OfferRepository;
import com.waffor.orderservice.entity.Offer;
import com.waffor.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OfferRepository offerRepository;
    private final OrderEventProducer orderEventProducer;
    private final org.springframework.jms.core.JmsTemplate jmsTemplate;

    @Override
    @Transactional
    public OrderResponseDto createOrder(OrderRequestDto request) {
        log.info("Creating order for customer: {}", request.getCustomerId());

        // 1. Generate Order Number
        String orderNumber = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // 2. Calculate Total Amount and Map items
        BigDecimal totalAmount = BigDecimal.ZERO;
        Order order = Order.builder()
                .orderNumber(orderNumber)
                .customerId(request.getCustomerId())
                .customerName(request.getCustomerName())
                .mobileNumber(request.getMobileNumber())
                .deliveryAddress(request.getDeliveryAddress())
                .paymentMethod(request.getPaymentMethod())
                .status("PLACED")
                .totalAmount(BigDecimal.ZERO) // Temporary value
                .build();

        if (request.getItems() != null) {
            for (var itemReq : request.getItems()) {
                BigDecimal itemTotal = itemReq.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
                totalAmount = totalAmount.add(itemTotal);

                OrderItem item = OrderItem.builder()
                        .productId(itemReq.getProductId())
                        .quantity(itemReq.getQuantity())
                        .price(itemReq.getPrice())
                        .build();
                order.addOrderItem(item);
            }
        }
        
        List<Offer> activeOffers = offerRepository.findByActive(true);
        if (!activeOffers.isEmpty()) {
            Offer activeOffer = activeOffers.get(0);
            BigDecimal discount = calculateDiscount(totalAmount, activeOffer.getText());
            totalAmount = totalAmount.subtract(discount);
            log.info("Applied active offer discount: {} on total. New total: {}", activeOffer.getText(), totalAmount);
        }
        
        order.setTotalAmount(totalAmount);

        // 3. Save to database
        Order savedOrder = orderRepository.save(order);
        log.info("[OrderService] Order #{} - PLACED", savedOrder.getId());


        // 4. Map to response DTO
        OrderResponseDto responseDto = mapToResponse(savedOrder);

        // 5. Publish to ActiveMQ
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(savedOrder.getId())
                .orderNumber(savedOrder.getOrderNumber())
                .customerId(savedOrder.getCustomerId())
                .totalAmount(savedOrder.getTotalAmount())
                .items(responseDto.getItems())
                .build();

        try {
            orderEventProducer.publishOrderCreated(event);
        } catch (Exception e) {
            log.error("JMS messaging failed, rolling back order persistence. ID: {}", savedOrder.getId(), e);
            throw e;
        }

        return responseDto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getAllOrders() {
        log.info("Retrieving all orders");
        return orderRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDto getOrderById(Long id) {
        log.info("Retrieving order with ID: {}", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + id));
        return mapToResponse(order);
    }

    @Override
    @Transactional
    public OrderResponseDto updateOrderStatus(Long id, String status) {
        log.info("Updating status of order ID: {} to: {}", id, status);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + id));
        order.setStatus(status.toUpperCase());
        Order updatedOrder = orderRepository.save(order);

        // Publish JMS response to unblock the Camunda delegate if manually approved by Admin
        if ("PAID".equalsIgnoreCase(status)) {
            com.waffor.orderservice.dto.event.PaymentResponseEvent event = com.waffor.orderservice.dto.event.PaymentResponseEvent.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .paymentNumber("PAY-MANUAL-" + order.getOrderNumber())
                .transactionId("TXN-MANUAL-" + java.util.UUID.randomUUID().toString().substring(0,8).toUpperCase())
                .amount(order.getTotalAmount())
                .status("SUCCESS")
                .build();
            log.info("Dispatched manual PaymentResponseEvent to unblock PaymentDelegate");
            jmsTemplate.convertAndSend(com.waffor.orderservice.config.JmsConfig.PAYMENT_RESPONSE_QUEUE, event);
        } else if ("READY".equalsIgnoreCase(status)) {
            com.waffor.orderservice.dto.event.KitchenResponseEvent event = com.waffor.orderservice.dto.event.KitchenResponseEvent.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .ticketNumber("KT-MANUAL-" + order.getOrderNumber())
                .status("READY")
                .estimatedPreparationTime(0)
                .build();
            log.info("Dispatched manual KitchenResponseEvent to unblock KitchenDelegate");
            jmsTemplate.convertAndSend(com.waffor.orderservice.config.JmsConfig.KITCHEN_RESPONSE_QUEUE, event);
        } else if ("DELIVERED".equalsIgnoreCase(status) || "DELIVERED_UNPAID".equalsIgnoreCase(status)) {
            com.waffor.orderservice.dto.event.DeliveryResponseEvent event = com.waffor.orderservice.dto.event.DeliveryResponseEvent.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .deliveryNumber("DLV-MANUAL-" + order.getOrderNumber())
                .driverId(1L)
                .driverName("System Admin Courier")
                .deliveryAddress(order.getDeliveryAddress())
                .status("DELIVERED".equalsIgnoreCase(status) ? "DELIVERED" : "DELIVERY_FAILED")
                .build();
            log.info("Dispatched manual DeliveryResponseEvent to unblock DeliveryDelegate");
            jmsTemplate.convertAndSend(com.waffor.orderservice.config.JmsConfig.DELIVERY_RESPONSE_QUEUE, event);
        }

        return mapToResponse(updatedOrder);
    }

    private BigDecimal calculateDiscount(BigDecimal originalAmount, String offerText) {
        if (offerText == null || offerText.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // Match percentage, e.g. "15%" or "15 %"
        java.util.regex.Pattern pctPattern = java.util.regex.Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*%");
        java.util.regex.Matcher pctMatcher = pctPattern.matcher(offerText);
        if (pctMatcher.find()) {
            double percent = Double.parseDouble(pctMatcher.group(1));
            return originalAmount.multiply(BigDecimal.valueOf(percent)).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
        }

        // Match flat discount, e.g. "flat 50" or "50 Rs off" or "₹50" or "50 rupees off"
        java.util.regex.Pattern flatPattern1 = java.util.regex.Pattern.compile("(?i)(?:flat|₹|rs\\.?)\\s*(\\d+(?:\\.\\d+)?)");
        java.util.regex.Matcher flatMatcher1 = flatPattern1.matcher(offerText);
        if (flatMatcher1.find()) {
            double flatVal = Double.parseDouble(flatMatcher1.group(1));
            return BigDecimal.valueOf(flatVal).min(originalAmount);
        }

        java.util.regex.Pattern flatPattern2 = java.util.regex.Pattern.compile("(?i)(\\d+(?:\\.\\d+)?)\\s*(?:rs|rupees|off)");
        java.util.regex.Matcher flatMatcher2 = flatPattern2.matcher(offerText);
        if (flatMatcher2.find()) {
            double flatVal = Double.parseDouble(flatMatcher2.group(1));
            return BigDecimal.valueOf(flatVal).min(originalAmount);
        }

        return BigDecimal.ZERO;
    }

    private OrderResponseDto mapToResponse(Order order) {
        List<OrderItemResponseDto> items = List.of();
        if (order.getOrderItems() != null) {
            items = order.getOrderItems().stream()
                    .map(item -> OrderItemResponseDto.builder()
                            .id(item.getId())
                            .productId(item.getProductId())
                            .quantity(item.getQuantity())
                            .price(item.getPrice())
                            .build())
                    .collect(Collectors.toList());
        }

        return OrderResponseDto.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .customerId(order.getCustomerId())
                .customerName(order.getCustomerName())
                .mobileNumber(order.getMobileNumber())
                .deliveryAddress(order.getDeliveryAddress())
                .paymentMethod(order.getPaymentMethod())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .items(items)
                .build();
    }
}
