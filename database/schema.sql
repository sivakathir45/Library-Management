-- ============================================================================
-- PSG iTech Library Resource and Queue Management System
-- Database Schema for Feature 1: User Management
-- Database: psgitech_library
-- ============================================================================

CREATE DATABASE IF NOT EXISTS psgitech_library
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE psgitech_library;

-- ----------------------------------------------------------------------------
-- Table: users
-- Stores all library users across roles: STUDENT, LIBRARIAN, ADMIN
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    role VARCHAR(20) NOT NULL,
    register_number VARCHAR(50) UNIQUE,
    employee_id VARCHAR(50) UNIQUE,
    department VARCHAR(50),
    `year` INT,
    active BOOLEAN DEFAULT TRUE,
    must_change_password BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_email (email),
    INDEX idx_user_reg_no (register_number),
    INDEX idx_user_emp_id (employee_id),
    INDEX idx_user_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- Table: remember_tokens
-- Stores hashed persistent authentication tokens for Remember Me functionality
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS remember_tokens (
    token_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_remember_token_hash (token_hash),
    INDEX idx_remember_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
