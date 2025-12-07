\c postgres
CREATE DATABASE warehouse;
\c warehouse

CREATE SCHEMA IF NOT EXISTS warehouse;

DROP TABLE IF EXISTS warehouse.stock_movements CASCADE;
DROP TABLE IF EXISTS warehouse.stock_items CASCADE;

CREATE TABLE warehouse.stock_items (id BIGSERIAL PRIMARY KEY, product_id BIGINT NOT NULL UNIQUE, quantity INTEGER NOT NULL DEFAULT 0, reserved INTEGER NOT NULL DEFAULT 0, last_stock_update TIMESTAMP DEFAULT NOW(), min_stock_level INTEGER NOT NULL DEFAULT 0, max_stock_level INTEGER);

CREATE TABLE warehouse.stock_movements (id BIGSERIAL PRIMARY KEY, product_id BIGINT NOT NULL, movement_type VARCHAR(20) NOT NULL, quantity INTEGER NOT NULL, reference VARCHAR(100), created_at TIMESTAMP DEFAULT NOW(), description VARCHAR(500));

CREATE INDEX IF NOT EXISTS idx_stock_items_product_id ON warehouse.stock_items(product_id);
CREATE INDEX IF NOT EXISTS idx_stock_movements_product_id ON warehouse.stock_movements(product_id);