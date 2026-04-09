-- Q-Hack Cloover: Postgres Init Script
-- Full schema + German mock/seed data
-- Run with: psql -U <user> -d <database> -f init.sql

BEGIN;

-- ============ DROP TABLES (reverse dependency order) ============


DROP TABLE IF EXISTS offers CASCADE;
DROP TABLE IF EXISTS products CASCADE;
DROP TABLE IF EXISTS properties CASCADE;
DROP TABLE IF EXISTS customers CASCADE;


-- ============ SCHEMA ============


CREATE TABLE customers
(
    id         SERIAL PRIMARY KEY,
    first_name VARCHAR(60) NOT NULL,
    last_name  VARCHAR(60) NOT NULL,
    birth_date DATE
);

CREATE TABLE properties
(
    id           SERIAL PRIMARY KEY,
    postcode     VARCHAR(10)  NOT NULL,
    street       VARCHAR(100) NOT NULL,
    city         VARCHAR(100) NOT NULL,
    house_number VARCHAR(10)  NOT NULL,
    customer_id  INT          NOT NULL REFERENCES customers (id) ON DELETE CASCADE,
    sunny_score  INT,
    explanation  TEXT,
    metadata     JSONB
);

CREATE TABLE products
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

CREATE TABLE offers
(
    id          SERIAL PRIMARY KEY,
    customer_id INT         NOT NULL REFERENCES customers (id) ON DELETE CASCADE,
    property_id INT         NOT NULL REFERENCES properties (id) ON DELETE CASCADE,
    product_id  INT         NOT NULL REFERENCES products (id) ON DELETE CASCADE,
    status      VARCHAR(20) NOT NULL DEFAULT 'PENDING'
);


-- ============ TEST DATA ============


INSERT INTO customers (first_name, last_name, birth_date)
VALUES ('Max', 'Mustermann', '1985-05-15'),
       ('Erika', 'Musterfrau', '1990-11-20'),
       ('Thomas', 'Schmidt', '1978-03-02'),
       ('Sabine', 'Fischer', '1995-07-12'),
       ('Klaus', 'Weber', '1965-12-30');

INSERT INTO products (name)
VALUES ('Wall Box'),
       ('Solar Panel'),
       ('Heat Pump');

INSERT INTO properties (postcode, street, city, house_number, customer_id, sunny_score, explanation, metadata)
VALUES ('10115', 'Friedrichstraße', 'Berlin', '1', 1, 85, 'Gute Dachausrichtung für PV.', '{"household_size": "4 Personen", "heating_type": "Gasheizung", "construction_year": "1995"}'),
       ('20457', 'Am Sandtorkai', 'Hamburg', '10', 2, 45, 'Verschattung durch Nachbargebäude.', '{"household_size": "2 Personen", "heating_type": "Fernwärme"}'),
       ('80331', 'Marienplatz', 'München', '1', 3, 90, 'Optimaler Ertrag möglich.', '{"existing_systems": "Solaranlage ohne Batterie", "annual_consumption_kwh": "3500"}');

COMMIT;
