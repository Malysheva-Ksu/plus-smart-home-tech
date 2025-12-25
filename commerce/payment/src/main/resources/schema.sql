CREATE TABLE IF NOT EXISTS payment
(
    payment_id     UUID PRIMARY KEY,
    product_price  NUMERIC(19, 2) NOT NULL CHECK (product_price >= 1),
    delivery_price NUMERIC(19, 2) NOT NULL CHECK (delivery_price >= 1),
    total_price    NUMERIC(19, 2) NOT NULL CHECK (total_price >= 1),
    payment_state  VARCHAR(50)    NOT NULL,
    order_id       UUID           NOT NULL
);