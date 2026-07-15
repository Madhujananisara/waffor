package com.waffor.orderservice.delegate;

import com.waffor.orderservice.entity.Order;
import com.waffor.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("cancelOrderDelegate")
@RequiredArgsConstructor
@Slf4j
public class CancelOrderDelegate implements JavaDelegate {

    private final OrderRepository orderRepository;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        Long orderId = (Long) execution.getVariable("orderId");
        log.warn("Executing CancelOrderDelegate: Cancelling Order ID {} due to payment failure.", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + orderId));
        
        order.setStatus("CANCELLED");
        orderRepository.save(order);
        log.warn("Order ID {} status updated to CANCELLED", orderId);
    }
}
