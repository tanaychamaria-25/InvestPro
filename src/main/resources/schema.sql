CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(190) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    cash_balance DECIMAL(15,2) NOT NULL DEFAULT 100000.00,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS investments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    symbol VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    type VARCHAR(30) NOT NULL,
    price DECIMAL(15,2) NOT NULL,
    description VARCHAR(500) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS holdings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    investment_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    average_cost DECIMAL(15,4) NOT NULL,
    UNIQUE KEY uq_user_investment (user_id, investment_id),
    CONSTRAINT fk_holdings_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_holdings_investment FOREIGN KEY (investment_id) REFERENCES investments(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    investment_id BIGINT NOT NULL,
    transaction_type VARCHAR(10) NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(15,2) NOT NULL,
    total_amount DECIMAL(15,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_transactions_user_created (user_id, created_at),
    CONSTRAINT fk_transactions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_transactions_investment FOREIGN KEY (investment_id) REFERENCES investments(id)
) ENGINE=InnoDB;
