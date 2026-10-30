-- ============================================================================
-- PSG iTech Library - Seed Data
-- Database: psgitech_library
-- Default Accounts for Testing:
-- 1. Admin: admin@psgitech.ac.in / AdminPassword@123
-- 2. Librarian: librarian@psgitech.ac.in / Librarian@123 (LIB101)
-- 3. New Librarian (First Login): meena.lib@psgitech.ac.in / InitialPassword@123 (LIB102)
-- 4. Student (Active): sivakathir.22cs@psgitech.ac.in / Student@123 (715522104045)
-- 5. Student (Active): ananya.23ai@psgitech.ac.in / Student@123 (715523105012)
-- 6. Student (Inactive): rahul.21me@psgitech.ac.in / Student@123 (715521102030)
-- ============================================================================

USE psgitech_library;

INSERT INTO users (name, email, password_hash, phone, role, register_number, employee_id, department, `year`, active, must_change_password)
VALUES 
-- Admin
('Admin Office', 'admin@psgitech.ac.in', '$2a$12$mohGNJyzR0i.BrCBGcaOauBw5uGH6otEWzwYKOIw9ethox8UmLOsO', '9876543210', 'ADMIN', NULL, NULL, NULL, NULL, TRUE, FALSE),

-- Established Librarian
('Dr. K. Ramanathan', 'librarian@psgitech.ac.in', '$2a$12$q9pWdhQeyqI1vvJz5fJubeJLLnizFXPoYX5k5zJtBTyhIGiMhlxku', '9876543211', 'LIBRARIAN', NULL, 'LIB101', NULL, NULL, TRUE, FALSE),

-- Newly Appointed Librarian (Requires first-login password change)
('Ms. S. Meena', 'meena.lib@psgitech.ac.in', '$2a$12$ljfDketAW/uNDlHvhXZkce5OQRn4qTRz1OwRWCyglBnjyzMn2bVNu', '9876543212', 'LIBRARIAN', NULL, 'LIB102', NULL, NULL, TRUE, TRUE),

-- Active Student (CSE, 3rd Year)
('Sivakathir S', 'sivakathir.22cs@psgitech.ac.in', '$2a$12$Jd.ZAaa7IFxQnxnXv.Fb6.0bG6pDBHZ913zg4pHnhRMH2OK.G2.qG', '9876543213', 'STUDENT', '715522104045', NULL, 'CSE', 3, TRUE, FALSE),

-- Active Student (AI&DS, 2nd Year)
('Ananya Ramesh', 'ananya.23ai@psgitech.ac.in', '$2a$12$Jd.ZAaa7IFxQnxnXv.Fb6.0bG6pDBHZ913zg4pHnhRMH2OK.G2.qG', '9876543214', 'STUDENT', '715523105012', NULL, 'AI&DS', 2, TRUE, FALSE),

-- Inactive Student (MECH, 4th Year - Account Deactivated)
('Rahul Kumar', 'rahul.21me@psgitech.ac.in', '$2a$12$Jd.ZAaa7IFxQnxnXv.Fb6.0bG6pDBHZ913zg4pHnhRMH2OK.G2.qG', '9876543215', 'STUDENT', '715521102030', NULL, 'MECH', 4, FALSE, FALSE)
ON DUPLICATE KEY UPDATE email=email;
