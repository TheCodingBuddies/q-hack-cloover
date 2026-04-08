-- Q-Hack Cloover: Postgres Init Script
-- Full schema + German mock/seed data
-- Run with: psql -U <user> -d <database> -f init.sql

BEGIN;

-- ============ DROP TABLES (reverse dependency order) ============

DROP TABLE IF EXISTS installer_products CASCADE;
DROP TABLE IF EXISTS installers CASCADE;
DROP TABLE IF EXISTS products CASCADE;
DROP TABLE IF EXISTS product_categories CASCADE;
DROP TABLE IF EXISTS customers CASCADE;
DROP TABLE IF EXISTS roof_properties CASCADE;
DROP TABLE IF EXISTS addresses CASCADE;

-- ============ SCHEMA ============

CREATE TABLE addresses (
    id              SERIAL PRIMARY KEY,
    street          VARCHAR(120)    NOT NULL,
    house_number    VARCHAR(10)     NOT NULL,
    postal_code     VARCHAR(10)     NOT NULL,
    city            VARCHAR(80)     NOT NULL,
    country         VARCHAR(50)     NOT NULL DEFAULT 'Deutschland',
    latitude        DECIMAL(9,6),
    longitude       DECIMAL(9,6)
);

CREATE TABLE roof_properties (
    id                      SERIAL PRIMARY KEY,
    address_id              INT             NOT NULL REFERENCES addresses(id),
    roof_orientation        VARCHAR(30),
    roof_tilt_degrees       INT,
    roof_area_sqm           DECIMAL(7,2),
    usable_roof_area_sqm    DECIMAL(7,2),
    roof_type               VARCHAR(50),
    roof_material           VARCHAR(50),
    basement_available      BOOLEAN,
    basement_area_sqm       DECIMAL(7,2),
    basement_notes          TEXT,
    CONSTRAINT uq_roof_address UNIQUE (address_id)
);

CREATE TABLE customers (
    id                                  SERIAL PRIMARY KEY,
    address_id                          INT             NOT NULL REFERENCES addresses(id),
    first_name                          VARCHAR(60)     NOT NULL,
    last_name                           VARCHAR(60)     NOT NULL,
    email                               VARCHAR(120)    NOT NULL,
    phone                               VARCHAR(30),
    annual_electricity_consumption_kwh  INT,
    annual_heating_consumption_kwh      INT,
    has_ev                              BOOLEAN         DEFAULT FALSE,
    current_heating_type                VARCHAR(50),
    interest_notes                      TEXT,
    created_at                          TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE TABLE product_categories (
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(80)     NOT NULL,
    description TEXT
);

CREATE TABLE products (
    id                      SERIAL PRIMARY KEY,
    category_id             INT             NOT NULL REFERENCES product_categories(id),
    name                    VARCHAR(120)    NOT NULL,
    manufacturer            VARCHAR(80),
    model_number            VARCHAR(60),
    price_net_eur           DECIMAL(10,2)   NOT NULL,
    vat_rate                DECIMAL(4,3)    NOT NULL DEFAULT 0.190,
    power_output_wp         INT,
    capacity_kwh            DECIMAL(7,2),
    efficiency_percent      DECIMAL(5,2),
    max_charging_power_kw   DECIMAL(6,2),
    cop                     DECIMAL(4,2),
    description             TEXT,
    in_stock                BOOLEAN         NOT NULL DEFAULT TRUE
);

CREATE TABLE installers (
    id                  SERIAL PRIMARY KEY,
    company_name        VARCHAR(120)    NOT NULL,
    contact_person      VARCHAR(80),
    email               VARCHAR(120),
    phone               VARCHAR(30),
    postal_code         VARCHAR(10),
    city                VARCHAR(80),
    service_radius_km   INT,
    description         TEXT
);

CREATE TABLE installer_products (
    id                          SERIAL PRIMARY KEY,
    installer_id                INT             NOT NULL REFERENCES installers(id),
    product_category_id         INT             NOT NULL REFERENCES product_categories(id),
    installation_price_net_eur  DECIMAL(10,2)   NOT NULL,
    installation_duration_hours DECIMAL(5,1)    NOT NULL,
    notes                       TEXT
);

-- ============ SEED DATA ============

-- ------------------------------------------------------------
-- addresses (20 rows)
-- ------------------------------------------------------------
INSERT INTO addresses (street, house_number, postal_code, city, latitude, longitude) VALUES
('Rosenstraße',           '12',   '80331', 'München',       48.135125,  11.581981),
('Kirchgasse',            '4a',   '20095', 'Hamburg',       53.550341,  10.000654),
('Schillerplatz',         '7',    '60313', 'Frankfurt am Main', 50.110924, 8.682127),
('Hauptstraße',           '88',   '50667', 'Köln',          50.938361,  6.959974),
('Königstraße',           '23',   '70173', 'Stuttgart',     48.775846,  9.182932),
('Waldstraße',            '5b',   '04109', 'Leipzig',       51.340199, 12.360103),
('Nürnberger Straße',     '31',   '90403', 'Nürnberg',      49.452030, 11.076751),
('Bertoldstraße',         '17',   '79098', 'Freiburg im Breisgau', 47.996132, 7.849436),
('Leinaustraße',          '9',    '30159', 'Hannover',      52.374478,  9.738553),
('Prager Straße',         '42',   '01069', 'Dresden',       51.049259, 13.738143),
('Windthorststraße',      '3',    '48143', 'Münster',       51.960665,  7.626135),
('Rohrbacher Straße',     '55',   '69115', 'Heidelberg',    49.398750,  8.672434),
('Schlossallee',          '2',    '14059', 'Berlin',        52.516274, 13.304210),
('Am Stadtpark',          '11',   '45128', 'Essen',         51.455643,  7.011555),
('Bahnhofstraße',         '66',   '44135', 'Dortmund',      51.514244,  7.468429),
('Gartenstraße',          '19',   '76133', 'Karlsruhe',     49.006890,  8.403653),
('Mozartstraße',          '8',    '86150', 'Augsburg',      48.370545,  10.897790),
('Habsburger Allee',      '34',   '68165', 'Mannheim',      49.483612,   8.469258),
('Domplatz',              '1',    '06108', 'Halle (Saale)', 51.482689, 11.969740),
('Neumarkt',              '27',   '99084', 'Erfurt',        50.978056, 11.029860);

-- ------------------------------------------------------------
-- roof_properties (20 rows, one per address)
-- ------------------------------------------------------------
INSERT INTO roof_properties (address_id, roof_orientation, roof_tilt_degrees, roof_area_sqm, usable_roof_area_sqm, roof_type, roof_material, basement_available, basement_area_sqm, basement_notes) VALUES
(1,  'Süd',      32, 120.00,  95.00, 'Satteldach',  'Ziegel',           TRUE,  28.00, 'Trocken, gut zugänglich'),
(2,  'Südwest',  25,  85.50,  68.00, 'Pultdach',    'Bitumen',          TRUE,  18.50, 'Leicht feucht, ausreichend Platz'),
(3,  'Süd',      10, 200.00, 180.00, 'Flachdach',   'Bitumen',          FALSE, NULL,  NULL),
(4,  'Ost',      38,  95.00,  72.00, 'Satteldach',  'Ziegel',           TRUE,  35.00, 'Trocken, groß'),
(5,  'Südost',   28, 110.00,  88.00, 'Walmdach',    'Betondachstein',   TRUE,  22.00, 'Trocken, mittelgroß'),
(6,  'West',     20, 130.00,  98.00, 'Satteldach',  'Ziegel',           FALSE, NULL,  NULL),
(7,  'Süd',      40,  78.00,  60.00, 'Satteldach',  'Ziegel',           TRUE,  15.00, 'Feucht, beengt'),
(8,  'Südwest',  33, 105.00,  83.00, 'Walmdach',    'Betondachstein',   TRUE,  25.00, 'Trocken, gut zugänglich'),
(9,  'Nord',     22,  92.00,  60.00, 'Pultdach',    'Blech',            TRUE,  20.00, 'Trocken, mittelgroß'),
(10, 'Südost',   30, 115.00,  92.00, 'Satteldach',  'Ziegel',           FALSE, NULL,  NULL),
(11, 'Süd',      15, 180.00, 162.00, 'Flachdach',   'Bitumen',          TRUE,  40.00, 'Trocken, sehr groß'),
(12, 'Ost',      35,  88.00,  65.00, 'Satteldach',  'Ziegel',           TRUE,  18.00, 'Trocken, zugänglich'),
(13, 'Süd',      45,  70.00,  52.00, 'Satteldach',  'Ziegel',           FALSE, NULL,  NULL),
(14, 'Südwest',  28, 140.00, 115.00, 'Walmdach',    'Betondachstein',   TRUE,  30.00, 'Leicht feucht, ausreichend Platz'),
(15, 'Süd',      12, 210.00, 190.00, 'Flachdach',   'Bitumen',          TRUE,  50.00, 'Trocken, sehr groß'),
(16, 'West',     30,  96.00,  74.00, 'Satteldach',  'Ziegel',           TRUE,  22.00, 'Trocken, mittelgroß'),
(17, 'Südost',   38,  83.00,  64.00, 'Satteldach',  'Betondachstein',   FALSE, NULL,  NULL),
(18, 'Süd',      20, 160.00, 140.00, 'Pultdach',    'Blech',            TRUE,  35.00, 'Trocken, groß'),
(19, 'Südwest',  25, 125.00, 100.00, 'Walmdach',    'Ziegel',           TRUE,  28.00, 'Feucht, beengt'),
(20, 'Ost',      42,  76.00,  57.00, 'Satteldach',  'Ziegel',           TRUE,  16.00, 'Trocken, zugänglich');

-- ------------------------------------------------------------
-- customers (25 rows; addresses 3, 7, 11, 14, 18 have 2 customers)
-- ------------------------------------------------------------
INSERT INTO customers (address_id, first_name, last_name, email, phone, annual_electricity_consumption_kwh, annual_heating_consumption_kwh, has_ev, current_heating_type, interest_notes, created_at) VALUES
(1,  'Markus',    'Hoffmann',   'markus.hoffmann@gmail.com',        '+49 89 12345678',   4200, 18000, FALSE, 'Gasheizung',   'Interesse an Solarpanel und Batteriespeicher',            '2024-01-15 09:23:00'),
(2,  'Sabine',    'Müller',     'sabine.mueller@web.de',            '+49 40 98765432',   3500, 15000, TRUE,  'Fernwärme',    'Hat Wallbox-Seite besucht, Interesse an PV-Anlage',       '2024-02-03 14:10:00'),
(3,  'Thomas',    'Schmidt',    'thomas.schmidt@t-online.de',       '+49 69 11223344',   5800, 22000, TRUE,  'Ölheizung',    'Interesse an Wärmepumpe und Solarpanel',                  '2024-01-28 11:05:00'),
(3,  'Claudia',   'Schmidt',    'claudia.schmidt@t-online.de',      '+49 69 11223345',   5800, 22000, FALSE, 'Ölheizung',    'Interesse an Batteriespeicher',                           '2024-01-28 11:07:00'),
(4,  'Andreas',   'Weber',      'a.weber@gmx.de',                   '+49 221 44556677',  3900, 17500, FALSE, 'Gasheizung',   'Solarpanel-Konfigurator genutzt',                         '2024-03-11 08:45:00'),
(5,  'Julia',     'Fischer',    'julia.fischer@yahoo.de',           '+49 711 22334455',  4500, 19000, TRUE,  'Wärmepumpe',   'Interesse an Wallbox und Erweiterung PV',                 '2024-02-20 16:30:00'),
(6,  'Stefan',    'Bauer',      'stefan.bauer@freenet.de',          '+49 341 55667788',  3200, 14000, FALSE, 'Gasheizung',   'Angebotsanfrage Solarpanel',                              '2024-03-05 10:15:00'),
(7,  'Petra',     'Koch',       'petra.koch@gmx.net',               '+49 911 33445566',  4100, 16500, FALSE, 'Ölheizung',    'Interesse an Wärmepumpe',                                 '2024-01-10 13:20:00'),
(7,  'Harald',    'Koch',       'harald.koch@gmx.net',              '+49 911 33445567',  4100, 16500, FALSE, 'Ölheizung',    'Interesse an Solarpanel',                                 '2024-01-10 13:22:00'),
(8,  'Christine', 'Richter',    'christine.richter@outlook.de',     '+49 761 66778899',  3700, 16000, TRUE,  'Gasheizung',   'Komplettpaket PV + Speicher + Wallbox gewünscht',          '2024-02-14 09:00:00'),
(9,  'Michael',   'Klein',      'michael.klein@posteo.de',          '+49 511 77889900',  4800, 20000, FALSE, 'Fernwärme',    'Interesse an Batteriespeicher',                           '2024-03-22 15:45:00'),
(10, 'Ursula',    'Wolf',       'ursula.wolf@web.de',               '+49 351 88990011',  3600, 15500, FALSE, 'Gasheizung',   'Solarpanel-Seite mehrfach besucht',                       '2024-01-05 12:30:00'),
(11, 'Rainer',    'Schröder',   'rainer.schroeder@gmail.com',       '+49 251 99001122',  5200, 21000, TRUE,  'Ölheizung',    'Interesse an Wärmepumpe und PV-Anlage',                   '2024-02-27 10:50:00'),
(11, 'Ingrid',    'Schröder',   'ingrid.schroeder@gmail.com',       '+49 251 99001123',  5200, 21000, FALSE, 'Ölheizung',    'Interesse an Batteriespeicher',                           '2024-02-27 10:55:00'),
(12, 'Frank',     'Neumann',    'frank.neumann@icloud.com',         '+49 6221 11223344', 4000, 17000, TRUE,  'Gasheizung',   'Wallbox-Vergleich angesehen',                             '2024-03-18 14:00:00'),
(13, 'Monika',    'Braun',      'monika.braun@gmx.de',              '+49 30 22334455',   3300, 14500, FALSE, 'Fernwärme',    'Interesse an kleiner PV-Anlage',                          '2024-01-22 08:15:00'),
(14, 'Dirk',      'Zimmermann', 'd.zimmermann@t-online.de',         '+49 201 33445566',  4600, 19500, TRUE,  'Gasheizung',   'Interesse an Komplettpaket',                              '2024-02-08 11:30:00'),
(14, 'Heike',     'Zimmermann', 'h.zimmermann@t-online.de',         '+49 201 33445567',  4600, 19500, FALSE, 'Gasheizung',   'Interesse an Wärmepumpe',                                 '2024-02-08 11:35:00'),
(15, 'Klaus',     'Hartmann',   'k.hartmann@gmail.com',             '+49 231 44556677',  5500, 23000, TRUE,  'Ölheizung',    'Großanlage für Gewerbe geplant, Interesse PV + Speicher', '2024-03-29 09:10:00'),
(16, 'Birgit',    'Krüger',     'birgit.krueger@web.de',            '+49 721 55667788',  3800, 16000, FALSE, 'Gasheizung',   'Solarpanel-Konfigurator genutzt',                         '2024-01-30 13:45:00'),
(17, 'Jörg',      'Lorenz',     'joerg.lorenz@freenet.de',          '+49 821 66778899',  4300, 18500, TRUE,  'Wärmepumpe',   'Interesse an PV und Wallbox',                             '2024-02-16 16:00:00'),
(18, 'Anke',      'Meyer',      'anke.meyer@posteo.de',             '+49 621 77889900',  3900, 17000, FALSE, 'Gasheizung',   'Interesse an Solarpanel',                                 '2024-03-07 10:20:00'),
(18, 'Tobias',    'Meyer',      'tobias.meyer@posteo.de',           '+49 621 77889901',  3900, 17000, TRUE,  'Gasheizung',   'Interesse an Wallbox und Batteriespeicher',               '2024-03-07 10:25:00'),
(19, 'Renate',    'Lehmann',    'renate.lehmann@gmail.com',         '+49 345 88990011',  4700, 20500, FALSE, 'Ölheizung',    'Wärmepumpe-Seite besucht',                                '2024-01-18 09:40:00'),
(20, 'Werner',    'Schulze',    'werner.schulze@gmx.net',           '+49 361 99001122',  4100, 17500, FALSE, 'Gasheizung',   'Interesse an Solarpanel und Wechselrichter',              '2024-02-25 15:10:00');

-- ------------------------------------------------------------
-- product_categories (5 rows)
-- ------------------------------------------------------------
INSERT INTO product_categories (name, description) VALUES
('Solarpanel',      'Photovoltaikmodule zur Stromerzeugung aus Sonnenenergie'),
('Wechselrichter',  'Wandeln Gleichstrom der Solarmodule in Wechselstrom um'),
('Batteriespeicher','Speichern überschüssigen Solarstrom für späteren Verbrauch'),
('Wallbox',         'Ladestation für Elektrofahrzeuge im Heimbereich'),
('Wärmepumpe',      'Effiziente Heizungssysteme auf Basis von Umweltwärme');

-- ------------------------------------------------------------
-- products (20 rows, 4 per category)
-- ------------------------------------------------------------

-- Solarpanel (category_id = 1)
INSERT INTO products (category_id, name, manufacturer, model_number, price_net_eur, vat_rate, power_output_wp, capacity_kwh, efficiency_percent, max_charging_power_kw, cop, description, in_stock) VALUES
(1, 'JinkoSolar Eagle 400W',        'JinkoSolar',  'JKM400M-6RL3-V',  189.00, 0.000, 400,  NULL, 21.30, NULL, NULL, 'Monokristallines Hochleistungsmodul, zuverlässiger Bestseller im Einstiegsbereich',              TRUE),
(1, 'LG NeON R 405W',               'LG',          'LG405Q1C-A6',     249.00, 0.000, 405,  NULL, 22.10, NULL, NULL, 'Bifaziales Premium-Modul mit hohem Ertrag auch bei Diffuslicht, 25 Jahre Garantie',             TRUE),
(1, 'Solarwatt Panel vision 375W',  'Solarwatt',   'SW375-60M-VI-GG', 219.00, 0.000, 375,  NULL, 20.80, NULL, NULL, 'Glas-Glas-Modul aus deutscher Produktion, besonders langlebig und widerstandsfähig',          TRUE),
(1, 'Meyer Burger White 400W',      'Meyer Burger','MBW400',           285.00, 0.000, 400,  NULL, 21.70, NULL, NULL, 'Premium-Modul mit HJT-Technologie, maximale Effizienz und geringstes Degradationsverhalten',   TRUE),

-- Wechselrichter (category_id = 2)
(2, 'SMA Sunny Boy 5.0',            'SMA',         'SB5.0-1VL-40',    899.00, 0.190, NULL, NULL, 97.00, NULL, NULL, 'Einphasiger Stringwechselrichter für kleine Dachanlagen, bewährte SMA-Qualität',               TRUE),
(2, 'Fronius Primo 8.0',            'Fronius',     'PRIMO-8.0-1',    1190.00, 0.190, NULL, NULL, 97.60, NULL, NULL, 'Einphasiger Wechselrichter mit WLAN und optionalem Smart Meter, österreichische Qualität',    TRUE),
(2, 'SMA Sunny Tripower 10.0',      'SMA',         'STP10.0-3AV-40', 1450.00, 0.190, NULL, NULL, 98.10, NULL, NULL, 'Dreiphasiger Wechselrichter für mittlere Anlagen, ShadeFix-Optimierung integriert',          TRUE),
(2, 'Huawei SUN2000-12KTL',         'Huawei',      'SUN2000-12KTL-M2',1320.00, 0.190, NULL, NULL, 98.40, NULL, NULL, 'Smart PV-Wechselrichter mit integriertem AFCI-Schutz und App-Steuerung',                     FALSE),

-- Batteriespeicher (category_id = 3)
(3, 'Sonnen eco 8',                 'Sonnen',      'sonneneco8-10',  7490.00, 0.190, NULL, 10.00, NULL, NULL, NULL, 'Intelligenter Heimspeicher mit Community-Funktion, 10 kWh nutzbarer Kapazität',              TRUE),
(3, 'BYD Battery-Box Premium HVS',  'BYD',         'BBPHVS10.2',     4990.00, 0.190, NULL, 10.20, NULL, NULL, NULL, 'Modularer Hochvolt-Speicher, erweiterbar, kompatibel mit vielen Wechselrichtern',            TRUE),
(3, 'SENEC Home V3 Hybrid',         'SENEC',       'SENEC.Home-V3-10',6890.00, 0.190, NULL,  7.50, NULL, NULL, NULL, 'Integrierter Hybrid-Speicher mit Notstrommodus und Cloud-Dienst',                           TRUE),
(3, 'Tesla Powerwall 2',            'Tesla',       'PW2',            8500.00, 0.190, NULL, 13.50, NULL, NULL, NULL, 'Wandmontierbarer Lithium-Ionen-Speicher, hohe Kapazität, integrierter Wechselrichter',       TRUE),

-- Wallbox (category_id = 4)
(4, 'go-e Charger HOME+ 11 kW',     'go-e',        'HOME-11',         549.00, 0.190, NULL, NULL, NULL,  11.00, NULL, 'Smarte WLAN-Wallbox mit App-Steuerung und PV-Überschussladen, einfache Montage',            TRUE),
(4, 'ABL eMH1 11 kW',               'ABL',         'eMH1-11',         489.00, 0.190, NULL, NULL, NULL,  11.00, NULL, 'Robuste Wallbox für den Heimbereich, deutsches Qualitätsprodukt, MID-Zähler optional',      TRUE),
(4, 'Wallbe Eco 2.0 22 kW',         'Wallbe',      'WB-ECO2-22',      699.00, 0.190, NULL, NULL, NULL,  22.00, NULL, 'Dreiphasige Hochleistungs-Wallbox für schnelles Laden, RFID-Authentifizierung',             FALSE),
(4, 'Easee Home 22 kW',             'Easee',       'EASEE-HOME-22',   749.00, 0.190, NULL, NULL, NULL,  22.00, NULL, 'Norwegisches Design, bis zu 22 kW, smarte Lastverteilung für Mehrfamilienhaus geeignet',    TRUE),

-- Wärmepumpe (category_id = 5)
(5, 'Vaillant aroTHERM plus 7 kW',  'Vaillant',    'VWL-7-5-AS-S2',  8990.00, 0.190, NULL, NULL, NULL, NULL,  4.50, 'Luft-Wasser-Wärmepumpe, besonders leise, hoher COP, für Bestandsgebäude geeignet',           TRUE),
(5, 'Bosch Compress 7000i AW 9 kW', 'Bosch',       'CS7000iAW9ORE',  9490.00, 0.190, NULL, NULL, NULL, NULL,  4.20, 'Effiziente Außeneinheit mit integriertem Pufferspeicher, einfache Installation',             TRUE),
(5, 'Stiebel Eltron WPL 15 ACS',    'Stiebel Eltron','WPL15ACS',    11200.00, 0.190, NULL, NULL, NULL, NULL,  4.80, 'Premium Luft-Wasser-Wärmepumpe mit Natural Cooling, für Niedrigenergiehäuser optimiert',     TRUE),
(5, 'Daikin Altherma 3 H 11 kW',    'Daikin',      'EBLA11D3W1',    10500.00, 0.190, NULL, NULL, NULL, NULL,  4.60, 'Japanische Qualität, intelligentes Energiemanagement, bis -25°C Außentemperatur betreibbar',  TRUE);

-- ------------------------------------------------------------
-- installers (8 rows)
-- ------------------------------------------------------------
INSERT INTO installers (company_name, contact_person, email, phone, postal_code, city, service_radius_km, description) VALUES
('SolarTech Bayern GmbH',           'Klaus Gruber',      'info@solartech-bayern.de',       '+49 89 44445555',   '80807', 'München',         80,  'Spezialist für PV-Anlagen und Batteriespeicher in Oberbayern, über 500 installierte Anlagen'),
('NordSonne Energie GmbH',          'Katrin Brandt',     'kontakt@nordsonne.de',            '+49 40 33334444',   '22767', 'Hamburg',         100, 'Erfahrener Installateur für PV und Wallboxen im norddeutschen Raum'),
('Rhein-Main Solar AG',             'Peter Hoffmann',    'service@rheinmain-solar.de',      '+49 69 66667777',   '60438', 'Frankfurt am Main',120, 'Komplettanbieter für Photovoltaik, Speicher und Wärmepumpen in Hessen'),
('EnergieHaus Schwaben',            'Lisa Vogt',         'info@energiehaus-schwaben.de',    '+49 711 77778888',  '70435', 'Stuttgart',       90,  'Familienbetrieb, spezialisiert auf Wärmepumpen und PV-Kombisysteme'),
('Grüne Energie Sachsen GmbH',      'Martin Neubauer',   'info@grüne-energie-sachsen.de',  '+49 341 22223333',  '04277', 'Leipzig',         110, 'Installateur für Ost- und Mitteldeutschland, Fokus auf Solarpanel und Speicher'),
('WestfalenSolar UG',               'Sabine Kröger',     'post@westfalensolar.de',          '+49 251 11112222',  '48155', 'Münster',         70,  'Regionaler Betrieb für Nordrhein-Westfalen, alle Produktkategorien'),
('Schwarzwald Solar & Wärme GmbH',  'Hans-Jörg Baumann', 'hallo@schwarzwald-solar.de',     '+49 761 88889999',  '79115', 'Freiburg im Breisgau', 85, 'Experte für Solarenergie und Wärmepumpen im Südwesten, BAFA-zertifiziert'),
('Mitte-Deutschland Energieservice','Yvonne Stark',      'kontakt@mitte-energieservice.de', '+49 345 55556666',  '06110', 'Halle (Saale)',   95,  'Installateur für Sachsen-Anhalt und Thüringen, PV-Komplettlösungen und Wallboxen');

-- ------------------------------------------------------------
-- installer_products (~28 rows)
-- ------------------------------------------------------------
-- SolarTech Bayern (installer_id=1): Solarpanel, Wechselrichter, Batteriespeicher, Wallbox
INSERT INTO installer_products (installer_id, product_category_id, installation_price_net_eur, installation_duration_hours, notes) VALUES
(1, 1, 1800.00, 8.0,  'Pauschalpreis für bis zu 10 Module, Gerüst inklusive'),
(1, 2,  450.00, 3.0,  'Installation inkl. Netzanmeldung und Inbetriebnahme'),
(1, 3,  600.00, 4.0,  'Wandmontage im Keller oder Hauswirtschaftsraum'),
(1, 4,  290.00, 2.5,  'Inklusive Unterputz-Zuleitung bis 10m'),

-- NordSonne Energie (installer_id=2): Solarpanel, Wechselrichter, Wallbox
(2, 1, 1950.00, 8.5,  'Pauschalpreis bis 12 Module, inklusive Dachziegel-Hooks'),
(2, 2,  480.00, 3.5,  'Anmeldung beim Netzbetreiber inklusive'),
(2, 4,  310.00, 2.5,  'Nur Unterputz-Installation, Außenmontage auf Anfrage'),

-- Rhein-Main Solar (installer_id=3): Solarpanel, Wechselrichter, Batteriespeicher, Wärmepumpe
(3, 1, 2100.00, 10.0, 'Erfahrene Teams für große Anlagen ab 10 kWp'),
(3, 2,  510.00, 3.5,  'Dreiphasige Wechselrichter bevorzugt, alle Fabrikate'),
(3, 3,  700.00, 5.0,  'Hochvolt- und Niedervolt-Systeme, Erdung inklusive'),
(3, 5, 3500.00, 16.0, 'Komplett-Einbau inkl. Hydraulikplan und Inbetriebnahme'),

-- EnergieHaus Schwaben (installer_id=4): Wärmepumpe, Solarpanel, Batteriespeicher
(4, 5, 3200.00, 14.0, 'BAFA-Förderantrag wird durch uns gestellt'),
(4, 1, 1750.00, 7.5,  'Spezialisiert auf Satteldächer, Flachdach auf Anfrage'),
(4, 3,  650.00, 4.5,  'Keller- oder Kellernischenmontage'),

-- Grüne Energie Sachsen (installer_id=5): Solarpanel, Wechselrichter, Batteriespeicher
(5, 1, 1700.00, 8.0,  'Günstige Regionalpreise, Gerüst extra berechnet'),
(5, 2,  420.00, 3.0,  'Anmeldung beim Netzbetreiber nach Absprache'),
(5, 3,  580.00, 4.0,  'Speicher-Nachrüstung zu bestehenden Anlagen möglich'),

-- WestfalenSolar (installer_id=6): Solarpanel, Wechselrichter, Wallbox, Wärmepumpe
(6, 1, 1850.00, 8.0,  'Bis zu 15 Module im Paushcalpreis enthalten'),
(6, 2,  460.00, 3.0,  'Inbetriebnahme und Monitoring-Einrichtung'),
(6, 4,  280.00, 2.0,  'Aufputz- und Unterputz-Installation möglich'),
(6, 5, 3400.00, 15.0, 'Fokus auf Bestandsgebäude und Sanierungen'),

-- Schwarzwald Solar & Wärme (installer_id=7): Solarpanel, Wärmepumpe, Batteriespeicher, Wallbox
(7, 1, 2000.00, 9.0,  'Schrägdach-Spezialist, inklusive Blitzschutz-Check'),
(7, 5, 3600.00, 16.0, 'Zertifizierter Wärmepumpen-Installateur, BAFA-Partner'),
(7, 3,  690.00, 4.5,  'Premium-Speicherlösungen für netzautarke Betriebe'),
(7, 4,  300.00, 2.5,  'Smarte Integration mit PV-Überschussladen'),

-- Mitte-Deutschland Energieservice (installer_id=8): Solarpanel, Wechselrichter, Wallbox
(8, 1, 1680.00, 7.5,  'Schnelle Abwicklung, Förderberatung inklusive'),
(8, 2,  430.00, 3.0,  'Alle gängigen Hersteller, Netzanmeldung inklusive'),
(8, 4,  270.00, 2.0,  'Günstigster Pauschalpreis in der Region');

COMMIT;
