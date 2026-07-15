-- Create kitchen_tickets table
CREATE TABLE IF NOT EXISTS kitchen_tickets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    ticket_number VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL,
    estimated_preparation_time INT NOT NULL DEFAULT 15,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_kitchen_tickets_ticket_number UNIQUE (ticket_number)
) ENGINE=InnoDB;

-- Indexes for performance optimization
CREATE INDEX idx_kitchen_tickets_order_id ON kitchen_tickets (order_id);
