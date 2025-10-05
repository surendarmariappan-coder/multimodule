CREATE TABLE IF NOT EXISTS Employees (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(320) NOT NULL,
    department_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_employees_email UNIQUE (email)
);

CREATE INDEX IF NOT EXISTS idx_employees_last_name ON Employees(last_name);
CREATE INDEX IF NOT EXISTS idx_employees_department ON Employees(department_id);
