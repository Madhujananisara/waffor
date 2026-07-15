-- Create deliveries table
CREATE TABLE IF NOT EXISTS deliveries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    delivery_number VARCHAR(50) NOT NULL,
    driver_id BIGINT,
    delivery_address VARCHAR(255) NOT NULL,
    status VARCHAR(30) NOT NULL,
    delivery_time TIMESTAMP NULL DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_deliveries_delivery_number UNIQUE (delivery_number)
) ENGINE=InnoDB;

-- Indexes for performance optimization
CREATE INDEX idx_deliveries_order_id ON deliveries (order_id);
CREATE INDEX idx_deliveries_driver_id ON deliveries (driver_id);
