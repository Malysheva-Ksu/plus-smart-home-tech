CREATE TABLE IF NOT EXISTS shopping_cart (
    id UUID PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    total_amount DECIMAL(10,2) DEFAULT 0,
    last_updated TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS shopping_cart_items (
    id UUID PRIMARY KEY,
    cart_id UUID NOT NULL REFERENCES shopping_cart(id),
    product_id UUID NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity >= 0),
    price DECIMAL(10,2) NOT NULL,
    UNIQUE(cart_id, product_id)
);