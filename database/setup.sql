-- This file creates/grants access only to the NEW InvestPro database.
-- It does not drop, rename, or alter library_management or any existing database.
CREATE DATABASE IF NOT EXISTS investpro
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

CREATE USER IF NOT EXISTS 'investpro_app'@'localhost' IDENTIFIED BY 'InvestProLocal!2026';
CREATE USER IF NOT EXISTS 'investpro_app'@'127.0.0.1' IDENTIFIED BY 'InvestProLocal!2026';
GRANT ALL PRIVILEGES ON investpro.* TO 'investpro_app'@'localhost';
GRANT ALL PRIVILEGES ON investpro.* TO 'investpro_app'@'127.0.0.1';
