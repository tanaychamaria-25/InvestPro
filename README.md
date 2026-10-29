# InvestPro — Investment Portfolio & Trading Simulator

A small web application for a Java OOP coursework project. Users can register/sign in, browse sample stocks, bonds and mutual funds, simulate buy/sell orders, monitor portfolio value and profit/loss, view transaction history, and export their transaction history as CSV.

> **Important:** This is a coursework simulation. Product prices are illustrative and are not live market prices. No real money or real trades are involved.

## Tech stack

- Java 21 language target
- Spring Boot web application (runs locally on port `8080`)
- MySQL 8.4 with JDBC / `JdbcTemplate`
- HTML, CSS and vanilla JavaScript
- Maven

## Requirements

- JDK 21 or newer
- Maven 3.9 or newer
- MySQL 8.4 running on `127.0.0.1:3307`
- VS Code (or another editor)

## Run it on the Mac used for this project

### 1. Keep MySQL 8.4 running

The current machine setup uses the separate MySQL 8.4 data directory `/opt/homebrew/var/mysql84`, socket `/tmp/mysql84.sock`, and port `3307`. Do **not** point MySQL 8.4 at `/opt/homebrew/var/mysql`; that directory belongs to another MySQL installation and must not be modified for this project.

If the MySQL 8.4 server is already running, continue to Step 2. To start the existing 8.4 data directory if it is stopped, open a separate Terminal window and run:

```bash
/opt/homebrew/opt/mysql@8.4/bin/mysqld_safe \
  --datadir=/opt/homebrew/var/mysql84 \
  --port=3307 \
  --socket=/tmp/mysql84.sock \
  --pid-file=/tmp/mysql84.pid \
  --log-error=/tmp/mysql84-recovery.err &
```

Wait a few seconds, then check:

```bash
/opt/homebrew/opt/mysql@8.4/bin/mysqladmin --socket=/tmp/mysql84.sock ping
```

It should print `mysqld is alive`.

### 2. Create the separate InvestPro database/user

In VS Code, open the **InvestPro project folder** and choose **Terminal → New Terminal**. Confirm the terminal is in the folder containing `pom.xml` and the `database` directory. Run:

```bash
/opt/homebrew/opt/mysql@8.4/bin/mysql --socket=/tmp/mysql84.sock -u root -p < database/setup.sql
```

Enter your existing MySQL root password when prompted. The script creates only the new `investpro` database and its dedicated local app user. It does **not** drop, rename, or modify `library_management`.

The tables and example investment products are created automatically when Spring Boot starts, through `src/main/resources/schema.sql` and `data.sql`.

### 3. Start the web application

In the VS Code terminal, run:

```bash
mvn spring-boot:run
```

The first run downloads Maven dependencies, so an internet connection is needed. When the log says the application has started, open this address in your browser:

**http://localhost:8080**

Create an account with your name, email, and a password of at least 6 characters. New accounts start with **₹1,00,000** of simulated cash.

### 4. Stop it

Return to the VS Code terminal running Spring Boot and press **Control + C**. This stops the web app; it does not delete the database.

## If something goes wrong

- **`mvn: command not found`** — confirm Maven is installed in Terminal with `mvn -v`; restart VS Code after installing Maven so its terminal refreshes the PATH.
- **Connection refused on port 3307** — check that the MySQL 8.4 server is running and that `mysqladmin ... ping` reports `mysqld is alive`.
- **Access denied for `investpro_app`** — rerun Step 2 using the MySQL 8.4 client and root account.
- **Port 8080 is already in use** — stop the other app using that port, or change `server.port=8080` in `application.properties` and then open the updated port in the browser.
- **A database error appears** — check the Spring Boot terminal for the exact error. Do not reset or delete either MySQL data directory to troubleshoot.

The local database app account is configured in `database/setup.sql` and `application.properties` for this coursework demo. Keep the app local; before any real deployment, change the credentials and add production-grade security controls.
