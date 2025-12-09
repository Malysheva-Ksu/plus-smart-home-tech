SELECT 'CREATE DATABASE warehouse' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'warehouse')\gexec;

SELECT 'CREATE DATABASE shopping_store' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'shopping_store')\gexec;

SELECT 'CREATE DATABASE shopping_cart' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'shopping_cart')\gexec;

SELECT 'CREATE DATABASE analyzer' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'analyzer')\gexec;