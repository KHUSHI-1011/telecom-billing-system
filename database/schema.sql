-- Telecom Billing System - Database Schema
-- Compatible with MySQL. For PostgreSQL, replace AUTO_INCREMENT with SERIAL.

-- Drop tables in reverse dependency order (for clean re-run)
DROP TABLE IF EXISTS bills;
DROP TABLE IF EXISTS usage_records;
DROP TABLE IF EXISTS customers;
DROP TABLE IF EXISTS plans;

-- Telecom plans: name, monthly price, data limit, call rate
CREATE TABLE plans (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    monthly_price DECIMAL(10, 2) NOT NULL,
    data_limit_gb DECIMAL(10, 2) NOT NULL COMMENT 'Data limit in GB per month',
    call_rate_per_min DECIMAL(10, 4) NOT NULL COMMENT 'Extra charge per minute over plan',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Customers: linked to a plan (nullable until assigned)
CREATE TABLE customers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    phone VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(200),
    plan_id INT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (plan_id) REFERENCES plans(id) ON DELETE SET NULL
);

-- Usage records: per customer per month (calls, SMS, data)
CREATE TABLE usage_records (
    id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    month INT NOT NULL CHECK (month >= 1 AND month <= 12),
    year INT NOT NULL,
    call_minutes DECIMAL(10, 2) DEFAULT 0,
    sms_count INT DEFAULT 0,
    data_usage_gb DECIMAL(10, 2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_customer_month_year (customer_id, month, year),
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
);

-- Bills: one per customer per month, linked to usage
CREATE TABLE bills (
    id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    usage_record_id INT NOT NULL,
    month INT NOT NULL,
    year INT NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    bill_breakdown VARCHAR(1000) COMMENT 'JSON or text breakdown',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE,
    FOREIGN KEY (usage_record_id) REFERENCES usage_records(id) ON DELETE CASCADE
);

-- Indexes for common queries
CREATE INDEX idx_customers_plan ON customers(plan_id);
CREATE INDEX idx_usage_customer_period ON usage_records(customer_id, year, month);
CREATE INDEX idx_bills_customer ON bills(customer_id);
CREATE INDEX idx_bills_period ON bills(year, month);
