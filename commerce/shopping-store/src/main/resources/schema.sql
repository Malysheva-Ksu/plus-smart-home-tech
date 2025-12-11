DROP TABLE IF EXISTS products

CREATE TABLE products (
    productId       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    productName     VARCHAR(255)   NOT NULL,
    description      TEXT           NOT NULL,
    imageSrc        VARCHAR(500),
    quantityState   VARCHAR(50)    NOT NULL CHECK (quantityState IN ('ENDED', 'FEW', 'ENOUGH', 'MANY')),
    productState    VARCHAR(50)    NOT NULL CHECK (productState IN ('ACTIVE', 'DEACTIVATE')),
    productCategory VARCHAR(50) CHECK (productCategory IN ('LIGHTING', 'CONTROL', 'SENSORS')),
    price            NUMERIC(19, 2) NOT NULL CHECK (price >= 1)
    );