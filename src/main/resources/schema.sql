-- Drop tables if they exist (for clean setup)
DROP TABLE IF EXISTS counter_break_logs;
DROP TABLE IF EXISTS queue_rotation_states;
DROP TABLE IF EXISTS daily_counter_states;
DROP TABLE IF EXISTS tokens;
DROP TABLE IF EXISTS counter_staff;
DROP TABLE IF EXISTS students;
DROP TABLE IF EXISTS service_counters;

-- 1. Students Table (Authentication for students)
CREATE TABLE students (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    roll_number VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_roll_number (roll_number),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Counter Staff Table (Authentication for counter staff)
CREATE TABLE counter_staff (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    staff_id VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_staff_id (staff_id),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Service Counters Table (Counter configuration)
CREATE TABLE service_counters (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name ENUM('A', 'B') UNIQUE NOT NULL,
    status ENUM('ACTIVE', 'ON_BREAK', 'UNAVAILABLE', 'CLOSED') NOT NULL DEFAULT 'ACTIVE',
    daily_limit INT NOT NULL DEFAULT 75,
    break_started_at TIMESTAMP NULL,
    break_reason VARCHAR(255) NULL,
    estimated_break_duration INT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Tokens Table (Main token/ticket entity)
CREATE TABLE tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    counter_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    token_number INT NOT NULL,
    token_code VARCHAR(10) UNIQUE NOT NULL,
    status ENUM('WAITING', 'SERVING', 'COMPLETED', 'DROPPED', 'RESCHEDULED') NOT NULL DEFAULT 'WAITING',
    service_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    served_at TIMESTAMP NULL,
    completed_at TIMESTAMP NULL,
    is_rescheduled BOOLEAN DEFAULT FALSE,
    original_service_date DATE NULL,
    
    FOREIGN KEY (counter_id) REFERENCES service_counters(id),
    FOREIGN KEY (student_id) REFERENCES students(id),
    
    INDEX idx_service_date_status (service_date, status),
    INDEX idx_counter_date (counter_id, service_date),
    INDEX idx_student_date (student_id, service_date),
    INDEX idx_token_code (token_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Daily Counter States Table (Token sequence manager)
CREATE TABLE daily_counter_states (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    counter_id BIGINT NOT NULL,
    service_date DATE NOT NULL,
    last_token_number INT NOT NULL DEFAULT 0,
    version BIGINT DEFAULT 0,
    
    FOREIGN KEY (counter_id) REFERENCES service_counters(id),
    UNIQUE KEY unique_counter_date (counter_id, service_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Queue Rotation States Table (Round-robin memory)
CREATE TABLE queue_rotation_states (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    service_date DATE UNIQUE NOT NULL,
    last_used_counter_id BIGINT NOT NULL,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (last_used_counter_id) REFERENCES service_counters(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Counter Break Logs Table (Audit trail for breaks)
CREATE TABLE counter_break_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    counter_id BIGINT NOT NULL,
    break_start TIMESTAMP NOT NULL,
    break_end TIMESTAMP NULL,
    reason VARCHAR(255) NULL,
    estimated_duration INT NULL,
    actual_duration INT NULL,
    
    FOREIGN KEY (counter_id) REFERENCES service_counters(id),
    INDEX idx_counter_date (counter_id, break_start)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;