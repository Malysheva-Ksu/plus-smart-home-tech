CREATE TABLE IF NOT EXISTS address
(
    id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    country VARCHAR(255) NOT NULL,
    city    VARCHAR(255) NOT NULL,
    street  VARCHAR(255) NOT NULL,
    house   VARCHAR(50)  NOT NULL,
    flat    VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS delivery
(
    delivery_id     UUID PRIMARY KEY NOT NULL,
    delivery_volume DOUBLE PRECISION NOT NULL,
    delivery_weight DOUBLE PRECISION NOT NULL,
    fragile         BOOLEAN          NOT NULL,
    address_from_id UUID REFERENCES address (id) ON DELETE CASCADE,
    address_to_id   UUID REFERENCES address (id) ON DELETE CASCADE,
    order_id        UUID             NOT NULL,
    delivery_state  VARCHAR(50) CHECK (delivery_state IN ('CREATED', 'IN_PROGRESS', 'DELIVERED', 'FAILED', 'CANCELLED'))
);