-- Q-Hack Cloover: Postgres Init Script
-- Full schema + German mock/seed data
-- Run with: psql -U <user> -d <database> -f init.sql

BEGIN;

-- ============ DROP TABLES (reverse dependency order) ============


DROP TABLE IF EXISTS customers CASCADE;


-- ============ SCHEMA ============


CREATE TABLE customers
(
    id         SERIAL PRIMARY KEY,
    first_name VARCHAR(60) NOT NULL,
    last_name  VARCHAR(60) NOT NULL,
    birth_date DATE
);


-- ============ TEST DATA ============


INSERT INTO customers (first_name, last_name, birth_date)
VALUES ('Max', 'Mustermann', '1985-05-15'),
       ('Erika', 'Musterfrau', '1990-11-20'),
       ('Thomas', 'Schmidt', '1978-03-02'),
       ('Sabine', 'Fischer', '1995-07-12'),
       ('Klaus', 'Weber', '1965-12-30');

COMMIT;
