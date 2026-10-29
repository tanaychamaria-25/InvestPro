package com.investpro.repository;

import com.investpro.model.User;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {
    private final JdbcTemplate jdbc;

    public UserRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public boolean existsByEmail(String email) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM users WHERE email = ?", Integer.class, email);
        return count != null && count > 0;
    }

    public void create(String fullName, String email, String passwordHash) {
        jdbc.update("INSERT INTO users (full_name, email, password_hash) VALUES (?, ?, ?)",
                fullName, email, passwordHash);
    }

    public Optional<String> findPasswordHashByEmail(String email) {
        List<String> results = jdbc.query("SELECT password_hash FROM users WHERE email = ?",
                (rs, rowNum) -> rs.getString("password_hash"), email);
        return results.stream().findFirst();
    }

    public Optional<User> findByEmail(String email) {
        List<User> results = jdbc.query(
                "SELECT id, full_name, email, cash_balance FROM users WHERE email = ?",
                (rs, rowNum) -> new User(rs.getLong("id"), rs.getString("full_name"),
                        rs.getString("email"), rs.getBigDecimal("cash_balance")), email);
        return results.stream().findFirst();
    }

    public Optional<User> findById(long id) {
        List<User> results = jdbc.query(
                "SELECT id, full_name, email, cash_balance FROM users WHERE id = ?",
                (rs, rowNum) -> new User(rs.getLong("id"), rs.getString("full_name"),
                        rs.getString("email"), rs.getBigDecimal("cash_balance")), id);
        return results.stream().findFirst();
    }
}
