-- Insert Counter A and Counter B
INSERT INTO service_counters (name, status, daily_limit) VALUES 
('A', 'ACTIVE', 75),
('B', 'ACTIVE', 75);

-- Insert sample counter staff
-- Password for both: Staff@123 (BCrypt hashed)
INSERT INTO counter_staff (staff_id, name, email, password) VALUES 
('STAFF001', 'Counter Staff A', 'staffa@college.edu', '$2a$10$gkP/Y2rGE.X0yjb.7wchnuuVSKQQq0MwUADAIPaNt.2piWzvHlDyi'),
('STAFF002', 'Counter Staff B', 'staffb@college.edu', '$2a$10$gkP/Y2rGE.X0yjb.7wchnuuVSKQQq0MwUADAIPaNt.2piWzvHlDyi');

-- Insert sample student (for testing)
-- Roll number: TEST001, Password: Test@123 (BCrypt hashed)
INSERT INTO students (roll_number, name, email, password) VALUES 
('TEST001', 'Test Student', 'test@college.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhkO');