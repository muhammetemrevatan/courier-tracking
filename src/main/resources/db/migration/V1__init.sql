CREATE SEQUENCE IF NOT EXISTS stores_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS couriers_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS courier_logs_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS courier_details_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE IF NOT EXISTS couriers (
    id BIGINT PRIMARY KEY DEFAULT NEXT VALUE FOR couriers_seq,
    name VARCHAR(255) NOT NULL,
    total_distance DOUBLE PRECISION NOT NULL DEFAULT 0,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS courier_details (
    id BIGINT PRIMARY KEY DEFAULT NEXT VALUE FOR courier_details_seq,
    courier_id BIGINT,
    last_lat DOUBLE PRECISION,
    last_lng DOUBLE PRECISION,
    last_time TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT fk_courier_details_courier
      FOREIGN KEY (courier_id) REFERENCES couriers(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS stores (
    id BIGINT PRIMARY KEY DEFAULT NEXT VALUE FOR stores_seq,
    name VARCHAR(255) NOT NULL UNIQUE,
    lat DOUBLE PRECISION NOT NULL,
    lng DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS courier_logs (
    id BIGINT PRIMARY KEY DEFAULT NEXT VALUE FOR courier_logs_seq,
    courier_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    entry_time TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT fk_courier_logs_courier FOREIGN KEY (courier_id) REFERENCES couriers(id) ON DELETE CASCADE,
    CONSTRAINT fk_courier_logs_store FOREIGN KEY (store_id) REFERENCES stores(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_courier_logs_courier_time
    ON courier_logs (courier_id, entry_time);

CREATE INDEX IF NOT EXISTS idx_courier_logs_courier_store_time
    ON courier_logs (courier_id, store_name, entry_time);
