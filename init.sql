-- Database schema for E-Commerce application

-- Create users table
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    user_type ENUM('admin', 'customer') NOT NULL
);

-- Create products table
CREATE TABLE products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    price DOUBLE NOT NULL,
    quantity INT NOT NULL
);

-- Insert default admin user
INSERT INTO users (username, password, user_type) VALUES ('admin', 'admin123', 'admin');

-- Insert sample products
INSERT INTO products (name, price, quantity) VALUES 
('Laptop', 55000.0, 5),
('Smartphone', 25000.0, 10),
('Headphones', 2000.0, 15),
('Keyboard', 1500.0, 8),
('Mouse', 900.0, 15),
('IPhone', 60000.0, 10);