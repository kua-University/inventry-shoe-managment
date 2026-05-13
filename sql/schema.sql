-- Table: Main Shoe Inventory
CREATE TABLE inventory_shoes (
    sku VARCHAR(50) PRIMARY KEY,
    brand VARCHAR(100) NOT NULL,
    model VARCHAR(100) NOT NULL,
    shoe_size DECIMAL(4,1) NOT NULL,
    stock_quantity INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table: Purchase Batches (The FIFO Engine)
CREATE TABLE purchase_batches (
    batch_id SERIAL PRIMARY KEY,
    sku VARCHAR(50) NOT NULL,
    purchase_price DECIMAL(10,2) NOT NULL,
    quantity_purchased INTEGER NOT NULL,
    quantity_remaining INTEGER NOT NULL, 
    purchased_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sku FOREIGN KEY (sku) REFERENCES inventory_shoes(sku) ON DELETE CASCADE
);
