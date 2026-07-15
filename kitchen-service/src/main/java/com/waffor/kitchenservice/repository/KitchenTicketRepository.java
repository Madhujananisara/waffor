package com.waffor.kitchenservice.repository;

import com.waffor.kitchenservice.entity.KitchenTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KitchenTicketRepository extends JpaRepository<KitchenTicket, Long> {
    Optional<KitchenTicket> findByTicketNumber(String ticketNumber);
    Optional<KitchenTicket> findByOrderId(Long orderId);
}
