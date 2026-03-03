-- Ocean View Resort Database
CREATE DATABASE IF NOT EXISTS oceanview_resort;
USE oceanview_resort;

DROP TABLE IF EXISTS reservations;
DROP TABLE IF EXISTS guests;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS rooms;

-- Admin/Staff users
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role ENUM('admin','staff') DEFAULT 'staff'
);

-- Guest accounts
CREATE TABLE guests (
    id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL,
    nic VARCHAR(20) NOT NULL,
    address TEXT NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Rooms
CREATE TABLE rooms (
    room_number VARCHAR(10) PRIMARY KEY,
    room_type VARCHAR(50) NOT NULL,
    is_available BOOLEAN DEFAULT TRUE
);

-- Reservations
CREATE TABLE reservations (
    reservation_number VARCHAR(20) PRIMARY KEY,
    guest_name VARCHAR(100) NOT NULL,
    address TEXT NOT NULL,
    contact_number VARCHAR(20) NOT NULL,
    room_number VARCHAR(10),
    room_type VARCHAR(50) NOT NULL,
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    status VARCHAR(20) DEFAULT 'Confirmed',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Default admin/staff
INSERT INTO users (username, password, full_name, role) VALUES
('admin',  SHA2('admin123', 256), 'Administrator', 'admin'),
('staff1', SHA2('staff123', 256), 'Staff Member',  'staff');

-- Rooms data
INSERT INTO rooms (room_number, room_type) VALUES
('101', 'Standard'), ('102', 'Standard'), ('103', 'Standard'),
('201', 'Deluxe'),   ('202', 'Deluxe'),   ('203', 'Deluxe'),
('301', 'Suite'),    ('302', 'Suite'),
('401', 'Ocean View'),('402', 'Ocean View');

SELECT 'Database setup complete!' AS message;
