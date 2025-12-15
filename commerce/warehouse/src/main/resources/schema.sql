CREATE TABLE IF NOT EXISTS warehouse_items
(
    product_id       UUID PRIMARY KEY,
    fragile          BOOLEAN          NOT NULL,
    quantity         INTEGER          NOT NULL,
    weight           DOUBLE PRECISION NOT NULL,
    dimension_width  DOUBLE PRECISION NOT NULL,
    dimension_height DOUBLE PRECISION NOT NULL,
    dimension_depth  DOUBLE PRECISION NOT NULL
);