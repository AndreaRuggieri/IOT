-- Crea il database "iot" se non esiste già
DROP DATABASE IF EXISTS iot;
CREATE DATABASE iot;

-- Seleziona il database "iot"
USE iot;

CREATE TABLE IF NOT EXISTS oxygen_sensor (
    id VARCHAR(5) NOT NULL,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    value INTEGER NOT NULL,
    PRIMARY KEY (id, timestamp)
);

CREATE TABLE IF NOT EXISTS cardio_sensor (
    id VARCHAR(5) NOT NULL,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    value INTEGER NOT NULL,
    PRIMARY KEY (id, timestamp)
);

CREATE TABLE IF NOT EXISTS troponin_sensor (
    id VARCHAR(5) NOT NULL,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    value DECIMAL(5, 2) NOT NULL,
    PRIMARY KEY (id, timestamp)
);

CREATE TABLE IF NOT EXISTS actuator (
    ip VARCHAR(45) PRIMARY KEY,
    type VARCHAR(40) NOT NULL,
    status INTEGER NOT NULL DEFAULT 0
);
-- Inserisci 10 valori nella tabella oxygen_sensor
INSERT INTO oxygen_sensor (id, timestamp, value) VALUES 
('o001', NOW(), 90),
('o001', DATE_ADD(NOW(), INTERVAL 1 SECOND), 90),
('o001', DATE_ADD(NOW(), INTERVAL 2 SECOND), 90),
('o001', DATE_ADD(NOW(), INTERVAL 3 SECOND), 90),
('o001', DATE_ADD(NOW(), INTERVAL 4 SECOND), 90);

-- Inserisci 10 valori nella tabella cardio_sensor
INSERT INTO cardio_sensor (id, timestamp, value) VALUES 
('c001', NOW(), 90),
('c001', DATE_ADD(NOW(), INTERVAL 1 SECOND), 90),
('c001', DATE_ADD(NOW(), INTERVAL 2 SECOND), 90),
('c001', DATE_ADD(NOW(), INTERVAL 3 SECOND), 90),
('c001', DATE_ADD(NOW(), INTERVAL 4 SECOND), 90);

-- Inserisci 10 valori nella tabella troponin_sensor
INSERT INTO troponin_sensor (id, timestamp, value) VALUES 
('t001', NOW(), 0.05),
('t001', DATE_ADD(NOW(), INTERVAL 1 SECOND), 0.05),
('t001', DATE_ADD(NOW(), INTERVAL 2 SECOND), 0.05),
('t001', DATE_ADD(NOW(), INTERVAL 3 SECOND), 0.05),
('t001', DATE_ADD(NOW(), INTERVAL 4 SECOND), 0.05);

INSERT INTO oxygen_sensor (id, timestamp, value) VALUES 
('o002', NOW(), 95),
('o002', DATE_ADD(NOW(), INTERVAL 1 SECOND), 95),
('o002', DATE_ADD(NOW(), INTERVAL 2 SECOND), 95),
('o002', DATE_ADD(NOW(), INTERVAL 3 SECOND), 95),
('o002', DATE_ADD(NOW(), INTERVAL 4 SECOND), 95);

-- Inserisci 10 valori nella tabella cardio_sensor
INSERT INTO cardio_sensor (id, timestamp, value) VALUES 
('c002', NOW(), 90),
('c002', DATE_ADD(NOW(), INTERVAL 1 SECOND), 90),
('c002', DATE_ADD(NOW(), INTERVAL 2 SECOND), 90),
('c002', DATE_ADD(NOW(), INTERVAL 3 SECOND), 90),
('c002', DATE_ADD(NOW(), INTERVAL 4 SECOND), 90);

-- Inserisci 10 valori nella tabella troponin_sensor
INSERT INTO troponin_sensor (id, timestamp, value) VALUES 
('t002', NOW(), 0.05),
('t002', DATE_ADD(NOW(), INTERVAL 1 SECOND), 0.05),
('t002', DATE_ADD(NOW(), INTERVAL 2 SECOND), 0.05),
('t002', DATE_ADD(NOW(), INTERVAL 3 SECOND), 0.05),
('t002', DATE_ADD(NOW(), INTERVAL 4 SECOND), 0.05);

INSERT INTO oxygen_sensor (id, timestamp, value) VALUES 
('o003', NOW(), 95),
('o003', DATE_ADD(NOW(), INTERVAL 1 SECOND), 95),
('o003', DATE_ADD(NOW(), INTERVAL 2 SECOND), 95),
('o003', DATE_ADD(NOW(), INTERVAL 3 SECOND), 95),
('o003', DATE_ADD(NOW(), INTERVAL 4 SECOND), 95);

-- Inserisci 10 valori nella tabella cardio_sensor
INSERT INTO cardio_sensor (id, timestamp, value) VALUES 
('c003', NOW(), 90),
('c003', DATE_ADD(NOW(), INTERVAL 1 SECOND), 90),
('c003', DATE_ADD(NOW(), INTERVAL 2 SECOND), 90),
('c003', DATE_ADD(NOW(), INTERVAL 3 SECOND), 90),
('c003', DATE_ADD(NOW(), INTERVAL 4 SECOND), 90);

-- Inserisci 10 valori nella tabella troponin_sensor
INSERT INTO troponin_sensor (id, timestamp, value) VALUES 
('t003', NOW(), 0.05),
('t003', DATE_ADD(NOW(), INTERVAL 1 SECOND), 0.05),
('t003', DATE_ADD(NOW(), INTERVAL 2 SECOND), 0.05),
('t003', DATE_ADD(NOW(), INTERVAL 3 SECOND), 0.05),
('t003', DATE_ADD(NOW(), INTERVAL 4 SECOND), 0.05);