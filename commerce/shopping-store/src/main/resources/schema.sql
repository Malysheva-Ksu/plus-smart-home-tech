\c postgres
CREATE DATABASE shopping-store;
\c shopping_store

DROP TABLE IF EXISTS shopping-store.products CASCADE;
DROP TABLE IF EXISTS shopping-store.categories CASCADE;

CREATE TABLE shopping-store.products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    category_id BIGINT,
    image_url VARCHAR(500),
    available BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE shopping-store.categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    parent_id BIGINT
);