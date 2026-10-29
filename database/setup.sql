-- Creates the InvestPro database if it does not already exist.
-- Does not drop or modify the library_management database.

CREATE DATABASE IF NOT EXISTS investpro
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

-- Create the application users with passwords configured separately.
-- Set the passwords securely in MySQL before running the application.
-- These statements do not reset passwords for existing users.

CREATE USER IF NOT EXISTS 'investpro_app'@'localhost';
CREATE USER IF NOT EXISTS 'investpro_app'@'127.0.0.1';

GRANT ALL PRIVILEGES ON investpro.* TO 'investpro_app'@'localhost';
GRANT ALL PRIVILEGES ON investpro.* TO 'investpro_app'@'127.0.0.1';
