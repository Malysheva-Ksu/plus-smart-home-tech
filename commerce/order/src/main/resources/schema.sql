CREATE TABLE IF NOT EXISTS address
(
    id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    country VARCHAR(255) NOT NULL,
    city    VARCHAR(255) NOT NULL,
    street  VARCHAR(255) NOT NULL,
    house   VARCHAR(50)  NOT NULL,
    flat    VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS orders
(
    id                  UUID PRIMARY KEY                 NOT NULL,
    username            VARCHAR(255)                     NOT NULL,
    state               VARCHAR(50) CHECK (state IN ('NEW', 'ON_PAYMENT', 'ON_DELIVERY', 'DONE', 'DELIVERED', 'ASSEMBLED',
                                            'PAID', 'COMPLETED', 'DELIVERY_FAILED', 'ASSEMBLY_FAILED', 'PAYMENT_FAILED',
                                            'PRODUCT_RETURNED', 'CANCELED')) NOT NULL,
    shopping_cart_id    UUID                             NOT NULL,
    payment_id          UUID,
    delivery_id         UUID,
    delivery_weight     DOUBLE PRECISION,
    delivery_volume     DOUBLE PRECISION,
    delivery_fragile    BOOLEAN,
    delivery_address_id UUID REFERENCES address (id) ON DELETE CASCADE,
    delivery_price      NUMERIC(19, 2),
    product_price       NUMERIC(19, 2),
    total_price         NUMERIC(19, 2)
);

CREATE TABLE IF NOT EXISTS order_products
(
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id   UUID REFERENCES orders (id) ON DELETE CASCADE,
    product_id UUID NOT NULL,
    quantity   INT  NOT NULL
);