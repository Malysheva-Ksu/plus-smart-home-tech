\c postgres
CREATE DATABASE shopping_cart;
\c shopping_cart

DROP TABLE IF EXISTS shopping_cart.carts CASCADE;
DROP TABLE IF EXISTS shopping_cart.cart_items CASCADE;

CREATE TABLE shopping_cart.carts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    total_amount DECIMAL(10,2) DEFAULT 0,
    last_updated TIMESTAMP DEFAULT NOW()
);

CREATE TABLE shopping_cart.cart_items (
    id BIGSERIAL PRIMARY KEY,
    cart_id BIGINT NOT NULL REFERENCES shopping_cart.carts(id),
    product_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    price DECIMAL(10,2) NOT NULL,
    UNIQUE(cart_id, product_id)
);