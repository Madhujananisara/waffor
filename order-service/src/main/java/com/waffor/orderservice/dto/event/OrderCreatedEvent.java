package com.waffor.orderservice.dto.event;

import com.waffor.orderservice.dto.OrderItemResponseDto;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCreatedEvent {
    private Long orderId;
    private String orderNumber;
    private Long customerId;
    private BigDecimal totalAmount;
    private List<OrderItemResponseDto> items;
}
