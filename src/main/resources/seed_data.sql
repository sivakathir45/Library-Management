INSERT INTO users (name, email, password_hash, phone, role, register_number, employee_id, department, `year`, active, must_change_password)
VALUES 
('Admin Office', 'admin@psgitech.ac.in', '$2a$12$mohGNJyzR0i.BrCBGcaOauBw5uGH6otEWzwYKOIw9ethox8UmLOsO', '9876543210', 'ADMIN', NULL, NULL, NULL, NULL, TRUE, FALSE),
('Dr. K. Ramanathan', 'librarian@psgitech.ac.in', '$2a$12$q9pWdhQeyqI1vvJz5fJubeJLLnizFXPoYX5k5zJtBTyhIGiMhlxku', '9876543211', 'LIBRARIAN', NULL, 'LIB101', NULL, NULL, TRUE, FALSE),
('Ms. S. Meena', 'meena.lib@psgitech.ac.in', '$2a$12$ljfDketAW/uNDlHvhXZkce5OQRn4qTRz1OwRWCyglBnjyzMn2bVNu', '9876543212', 'LIBRARIAN', NULL, 'LIB102', NULL, NULL, TRUE, TRUE),
('Sivakathir S', 'sivakathir.22cs@psgitech.ac.in', '$2a$12$Jd.ZAaa7IFxQnxnXv.Fb6.0bG6pDBHZ913zg4pHnhRMH2OK.G2.qG', '9876543213', 'STUDENT', '715522104045', NULL, 'CSE', 3, TRUE, FALSE),
('Ananya Ramesh', 'ananya.23ai@psgitech.ac.in', '$2a$12$Jd.ZAaa7IFxQnxnXv.Fb6.0bG6pDBHZ913zg4pHnhRMH2OK.G2.qG', '9876543214', 'STUDENT', '715523105012', NULL, 'AI&DS', 2, TRUE, FALSE),
('Rahul Kumar', 'rahul.21me@psgitech.ac.in', '$2a$12$Jd.ZAaa7IFxQnxnXv.Fb6.0bG6pDBHZ913zg4pHnhRMH2OK.G2.qG', '9876543215', 'STUDENT', '715521102030', NULL, 'MECH', 4, FALSE, FALSE);

INSERT INTO books (book_id, isbn, title, author, publisher, edition, publication_year, category, department, description, cover_image_url, is_archived) VALUES
(1, '978-0262033848', 'Introduction to Algorithms', 'Thomas H. Cormen, Charles E. Leiserson, Ronald L. Rivest, Clifford Stein', 'MIT Press', '4th Edition', 2022, 'Data Structures', 'CSE', 'The definitive modern algorithms textbook covering asymptotic analysis, sorting, dynamic programming, greedy algorithms, and graph theory.', 'https://covers.openlibrary.org/b/isbn/9780262033848-L.jpg', FALSE),
(2, '978-0132350884', 'Clean Code: A Handbook of Agile Software Craftsmanship', 'Robert C. Martin', 'Prentice Hall', '1st Edition', 2008, 'Programming', 'CSE', 'Best practices and clean architecture patterns for writing robust, maintainable, and readable software systems.', 'https://covers.openlibrary.org/b/isbn/9780132350884-L.jpg', FALSE),
(3, '978-0136042594', 'Artificial Intelligence: A Modern Approach', 'Stuart Russell, Peter Norvig', 'Pearson', '4th Edition', 2020, 'Artificial Intelligence', 'AI&DS', 'Comprehensive university reference on search algorithms, probabilistic reasoning, machine learning, and natural language processing.', 'https://covers.openlibrary.org/b/isbn/9780136042594-L.jpg', FALSE),
(4, '978-0132126953', 'Computer Networks', 'Andrew S. Tanenbaum, David J. Wetherall', 'Pearson', '5th Edition', 2011, 'Computer Science', 'CSE', 'Covers protocol architecture, physical layers, data links, wireless transmission, routing algorithms, and internet security.', 'https://covers.openlibrary.org/b/isbn/9780132126953-L.jpg', FALSE),
(5, '978-1119456339', 'Operating System Concepts', 'Abraham Silberschatz, Peter B. Galvin, Greg Gagne', 'Wiley', '10th Edition', 2018, 'Computer Science', 'CSE', 'Core foundational textbook covering process scheduling, multi-threading, concurrency, memory management, and file systems.', 'https://covers.openlibrary.org/b/isbn/9781119456339-L.jpg', FALSE),
(6, '978-0078022159', 'Database System Concepts', 'Abraham Silberschatz, Henry F. Korth, S. Sudarshan', 'McGraw-Hill Education', '7th Edition', 2019, 'Computer Science', 'IT', 'Comprehensive guide to relational data models, SQL queries, transaction processing, indexing, and distributed databases.', 'https://covers.openlibrary.org/b/isbn/9780078022159-L.jpg', FALSE),
(7, '978-0199464689', 'Linear Algebra and Its Applications', 'Gilbert Strang', 'Cengage Learning', '4th Edition', 2016, 'Mathematics', 'GENERAL', 'Essential linear algebra concepts for computing, including vector spaces, matrix factorization, eigenvalues, and SVD.', 'https://covers.openlibrary.org/b/isbn/9780199464689-L.jpg', FALSE),
(8, '978-0199339136', 'Microelectronic Circuits', 'Adel S. Sedra, Kenneth C. Smith', 'Oxford University Press', '7th Edition', 2014, 'Electronics', 'ECE', 'Foundational curriculum textbook analyzing operational amplifiers, diode circuits, MOSFETs, and analog signal processing.', 'https://covers.openlibrary.org/b/isbn/9780199339136-L.jpg', FALSE),
(9, '978-1260440232', 'Java: The Complete Reference', 'Herbert Schildt', 'McGraw-Hill Education', '12th Edition', 2021, 'Programming', 'IT', 'Authoritative guide to Java programming syntax, OOP principles, collections framework, concurrency utilities, and I/O streams.', 'https://covers.openlibrary.org/b/isbn/9781260440232-L.jpg', FALSE),
(10, '978-0134685991', 'Effective Java', 'Joshua Bloch', 'Addison-Wesley Professional', '3rd Edition', 2018, 'Programming', 'CSE', 'Industry standard manual of best programming practices in the Java programming language.', 'https://covers.openlibrary.org/b/isbn/9780134685991-L.jpg', FALSE);

INSERT INTO book_copies (copy_code, book_id, status) VALUES
('PSG-BK1-C01', 1, 'AVAILABLE'),
('PSG-BK1-C02', 1, 'AVAILABLE'),
('PSG-BK1-C03', 1, 'AVAILABLE'),
('PSG-BK1-C04', 1, 'AVAILABLE'),

('PSG-BK2-C01', 2, 'AVAILABLE'),
('PSG-BK2-C02', 2, 'AVAILABLE'),
('PSG-BK2-C03', 2, 'AVAILABLE'),

('PSG-BK3-C01', 3, 'AVAILABLE'),
('PSG-BK3-C02', 3, 'AVAILABLE'),
('PSG-BK3-C03', 3, 'AVAILABLE'),

('PSG-BK4-C01', 4, 'AVAILABLE'),
('PSG-BK4-C02', 4, 'AVAILABLE'),
('PSG-BK4-C03', 4, 'AVAILABLE'),

('PSG-BK5-C01', 5, 'AVAILABLE'),
('PSG-BK5-C02', 5, 'AVAILABLE'),
('PSG-BK5-C03', 5, 'AVAILABLE'),

('PSG-BK6-C01', 6, 'AVAILABLE'),
('PSG-BK6-C02', 6, 'AVAILABLE'),
('PSG-BK6-C03', 6, 'AVAILABLE'),

('PSG-BK7-C01', 7, 'AVAILABLE'),
('PSG-BK7-C02', 7, 'AVAILABLE'),

('PSG-BK8-C01', 8, 'AVAILABLE'),
('PSG-BK8-C02', 8, 'AVAILABLE'),

('PSG-BK9-C01', 9, 'AVAILABLE'),
('PSG-BK9-C02', 9, 'AVAILABLE'),
('PSG-BK9-C03', 9, 'AVAILABLE'),

('PSG-BK10-C01', 10, 'AVAILABLE'),
('PSG-BK10-C02', 10, 'AVAILABLE');
