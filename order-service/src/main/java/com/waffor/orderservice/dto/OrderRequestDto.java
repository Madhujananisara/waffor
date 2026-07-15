package com.waffor.orderservice.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequestDto {
    private Long customerId;
    private String customerName;
    private String mobileNumber;
    private String deliveryAddress;
    private String paymentMethod;
    private List<OrderItemRequestDto> items;
}
