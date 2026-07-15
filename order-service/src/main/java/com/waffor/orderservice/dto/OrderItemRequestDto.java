package com.waffor.orderservice.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemRequestDto {
    private Long productId;
    private Integer quantity;
    private BigDecimal price;
}
