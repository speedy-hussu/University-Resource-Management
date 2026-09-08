-- ==========================================================
-- University Resource Management System (URMS) - Seed Data
-- ==========================================================

-- 0. Default Operator: admin / admin123 (BCrypt hash)
INSERT OR IGNORE INTO Users (user_id, username, password_hash, full_name, is_active) VALUES
(1, 'admin', '$2a$10$wN2a3R2V6X0l6pT3N2Y8u.C5tqF5b9vR4M8t9V3z5x1y7q2w3e4r', 'System Administrator', 1);

-- 1. Categories
INSERT OR IGNORE INTO Categories (category_id, category_name, description, is_active) VALUES
(1, 'Lab Equipment', 'Electronics, IoT, and hardware instruments', 1),
(2, 'Audio-Visual', 'Projectors, camcorders, and display gear', 1),
(3, 'Campus Venues', 'Auditoriums, seminar halls, and meeting rooms', 1),
(4, 'Consumable Supplies', '3D printing filaments, jumper wires, components', 1);

-- 2. Resources
INSERT OR IGNORE INTO Resources (resource_id, resource_name, category_id, resource_type, location, total_quantity, available_quantity, is_active) VALUES
(1, 'Rigol Digital Oscilloscope (50MHz)', 1, 'EQUIPMENT', 'Electronics Lab Bench 2', 5, 4, 1),
(2, 'Siglent Function Generator (25MHz)', 1, 'EQUIPMENT', 'Electronics Lab Shelf 3', 4, 4, 1),
(3, 'Epson PowerLite 1080p Projector', 2, 'EQUIPMENT', 'Media Locker A1', 6, 5, 1),
(4, 'Sony 4K Camcorder Kit', 2, 'EQUIPMENT', 'Media Locker A2', 3, 3, 1),
(5, 'Main Seminar Hall A (200 Seats)', 3, 'VENUE', 'Admin Block Floor 2', 1, 1, 1),
(6, 'PLA 3D Printer Filament 1kg Spool', 4, 'CONSUMABLE', '3D Fabrication Lab', 20, 18, 1),
(7, 'Male-to-Male Jumper Wire Pack (50pcs)', 4, 'CONSUMABLE', 'IoT Research Lab', 50, 45, 1);

-- 3. Allocations (Active / Returned sample records)
INSERT OR IGNORE INTO Allocations (allocation_id, resource_id, borrower_id, borrower_name, borrower_type, quantity, issue_date, due_date, return_date, status, issued_by, remarks) VALUES
(1, 1, '22BCE104', 'Aryan Verma', 'STUDENT', 1, '2026-09-08 10:00:00', '2026-09-08 14:00:00', NULL, 'ACTIVE', 'admin', 'DSP Lab filter circuit experiment'),
(2, 3, 'FAC-CSE-014', 'Dr. Rajesh Sharma', 'FACULTY', 1, '2026-09-08 09:30:00', '2026-09-08 13:30:00', NULL, 'ACTIVE', 'admin', 'Department guest lecture'),
(3, 6, '22BEC045', 'Kavya Nair', 'STUDENT', 2, '2026-09-07 11:00:00', '2026-09-07 17:00:00', '2026-09-07 16:45:00', 'RETURNED', 'admin', 'Capstone project prototyping');
