DROP TABLE IF EXISTS warehouse.stock_items CASCADE;
DROP TABLE IF EXISTS warehouse.stock_movements CASCADE;

CREATE TABLE warehouse.stock_items (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL UNIQUE,
    quantity INTEGER NOT NULL DEFAULT 0,
    reserved INTEGER NOT NULL DEFAULT 0,
    last_stock_update TIMESTAMP DEFAULT NOW()
    min_stock_level INTEGER NOT NULL DEFAULT 0,
    max_stock_level INTEGER
);

CREATE TABLE warehouse.stock_movements (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    movement_type VARCHAR(20) NOT NULL, -- IN, OUT, RESERVE, RELEASE
    quantity INTEGER NOT NULL,
    reference VARCHAR(100), -- order_id, etc.
    created_at TIMESTAMP DEFAULT NOW(),
    description VARCHAR(500)
);