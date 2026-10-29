package com.investpro.service;

import com.investpro.model.User;
import com.investpro.repository.UserRepository;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/** Portfolio queries return a fresh snapshot from the database instead of caching money values. */
@Service
public class PortfolioService {
    private final JdbcTemplate jdbc;
    private final UserRepository users;

    public PortfolioService(JdbcTemplate jdbc, UserRepository users) {
        this.jdbc = jdbc;
        this.users = users;
    }

    public Map<String, Object> dashboard(long userId) {
        User user = users.findById(userId).orElseThrow(() -> new IllegalArgumentException("Account not found."));
        List<Map<String, Object>> holdings = jdbc.queryForList(
                "SELECT h.id, i.id AS investmentId, i.symbol, i.name, i.type, i.price AS currentPrice, " +
                "h.quantity, h.average_cost AS averageCost, " +
                "(i.price * h.quantity) AS marketValue, " +
                "((i.price - h.average_cost) * h.quantity) AS profitLoss " +
                "FROM holdings h JOIN investments i ON i.id = h.investment_id " +
                "WHERE h.user_id = ? ORDER BY marketValue DESC, i.name", userId);
        List<Map<String, Object>> recent = jdbc.queryForList(
                "SELECT t.id, t.created_at AS createdAt, t.transaction_type AS action, i.symbol, i.name, " +
                "t.quantity, t.price, t.total_amount AS totalAmount " +
                "FROM transactions t JOIN investments i ON i.id = t.investment_id " +
                "WHERE t.user_id = ? ORDER BY t.created_at DESC, t.id DESC LIMIT 8", userId);

        BigDecimal marketValue = BigDecimal.ZERO;
        BigDecimal unrealizedProfitLoss = BigDecimal.ZERO;
        for (Map<String, Object> holding : holdings) {
            marketValue = marketValue.add(asDecimal(holding.get("marketValue")));
            unrealizedProfitLoss = unrealizedProfitLoss.add(asDecimal(holding.get("profitLoss")));
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("user", user);
        result.put("cashBalance", user.getCashBalance());
        result.put("holdings", holdings);
        result.put("recentTransactions", recent);
        result.put("marketValue", marketValue);
        result.put("totalEquity", user.getCashBalance().add(marketValue));
        result.put("unrealizedProfitLoss", unrealizedProfitLoss);
        return result;
    }

    public List<Map<String, Object>> transactions(long userId) {
        return jdbc.queryForList(
                "SELECT t.id, t.created_at AS createdAt, t.transaction_type AS action, i.symbol, i.name, " +
                "t.quantity, t.price, t.total_amount AS totalAmount " +
                "FROM transactions t JOIN investments i ON i.id = t.investment_id " +
                "WHERE t.user_id = ? ORDER BY t.created_at DESC, t.id DESC LIMIT 250", userId);
    }

    private BigDecimal asDecimal(Object value) {
        return value instanceof BigDecimal decimal ? decimal : new BigDecimal(String.valueOf(value));
    }
}
