-- MySQL Observability Demo - Database Initialization Script

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    city VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_city (city)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Create orders table
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    product VARCHAR(100) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Insert sample users
INSERT INTO users (name, email, city) VALUES
('Alice Johnson', 'alice@example.com', 'New York'),
('Bob Smith', 'bob@example.com', 'Los Angeles'),
('Charlie Brown', 'charlie@example.com', 'Chicago'),
('Diana Prince', 'diana@example.com', 'Houston'),
('Ethan Hunt', 'ethan@example.com', 'Phoenix'),
('Fiona Green', 'fiona@example.com', 'Philadelphia'),
('George White', 'george@example.com', 'San Antonio'),
('Hannah Blue', 'hannah@example.com', 'San Diego'),
('Ian Black', 'ian@example.com', 'Dallas'),
('Julia Red', 'julia@example.com', 'San Jose');

-- Insert sample orders
INSERT INTO orders (user_id, product, amount, status) VALUES
(1, 'Laptop', 1299.99, 'COMPLETED'),
(1, 'Mouse', 29.99, 'COMPLETED'),
(2, 'Keyboard', 79.99, 'PENDING'),
(2, 'Monitor', 299.99, 'SHIPPED'),
(3, 'Headphones', 149.99, 'COMPLETED'),
(3, 'Webcam', 89.99, 'PENDING'),
(4, 'Desk Chair', 249.99, 'COMPLETED'),
(5, 'Standing Desk', 599.99, 'SHIPPED'),
(6, 'USB Hub', 39.99, 'COMPLETED'),
(7, 'Cable Organizer', 19.99, 'PENDING'),
(8, 'Laptop Stand', 49.99, 'COMPLETED'),
(9, 'External SSD', 129.99, 'SHIPPED'),
(10, 'Wireless Charger', 34.99, 'COMPLETED'),
(1, 'Phone Case', 24.99, 'PENDING'),
(2, 'Screen Protector', 14.99, 'COMPLETED');

-- Display summary
SELECT 'Database initialized successfully!' AS status;
SELECT COUNT(*) AS total_users FROM users;
SELECT COUNT(*) AS total_orders FROM orders;
