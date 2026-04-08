-- Q-Hack Cloover: Postgres Init Script
-- Full schema + German mock/seed data
-- Run with: psql -U <user> -d <database> -f init.sql

BEGIN;

-- ============ DROP TABLES (reverse dependency order) ============

DROP TABLE IF EXISTS offers CASCADE;
DROP TABLE IF EXISTS properties CASCADE;
DROP TABLE IF EXISTS customers CASCADE;
DROP TABLE IF EXISTS products CASCADE;


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
    explanation  TEXT
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
VALUES ('Max',    'Mustermann', '1985-05-15'),
       ('Erika',  'Musterfrau', '1990-11-20'),
       ('Thomas', 'Schmidt',    '1978-03-02'),
       ('Sabine', 'Fischer',    '1995-07-12'),
       ('Klaus',  'Weber',      '1965-12-30');

INSERT INTO properties (postcode, street, city, house_number, customer_id, sunny_score, explanation)
VALUES
    ('10115', 'Friedrichstraße',   'Berlin',    '42',  1, 82, 'Südausrichtung mit wenig Verschattung – sehr gute Bedingungen für Photovoltaik.'),
    ('20095', 'Mönckebergstraße',  'Hamburg',   '7',   2, 67, 'Leichte West-Neigung, gelegentliche Verschattung durch Nachbargebäude.'),
    ('80331', 'Marienplatz',       'München',   '3',   3, 91, 'Optimale Südlage auf erhöhtem Gelände, kaum Schatten – ausgezeichnetes Solarpotenzial.'),
    ('70173', 'Königstraße',       'Stuttgart', '15',  4, 74, 'Gute Sonneneinstrahlung, leichte Verschattung durch Dachaufbauten.'),
    ('50667', 'Schildergasse',     'Köln',      '22',  5, 55, 'Innerstädtische Lage mit mäßiger Verschattung durch umliegende Gebäude.');

INSERT INTO products (name)
VALUES ('Wall Box'),
       ('Solar Panel'),
       ('Heat Pump');

COMMIT;
