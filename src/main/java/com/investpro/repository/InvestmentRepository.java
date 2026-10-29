package com.investpro.repository;

import com.investpro.model.Investment;
import com.investpro.model.InvestmentFactory;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class InvestmentRepository {
    private static final String SELECT_COLUMNS =
            "SELECT id, symbol, name, type, price, description FROM investments";
    private final JdbcTemplate jdbc;

    public InvestmentRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public List<Investment> findAll() {
        return jdbc.queryForList(SELECT_COLUMNS + " ORDER BY type, name")
                .stream().map(InvestmentFactory::fromRow).toList();
    }

    public Investment findById(long id) {
        List<Map<String, Object>> rows = jdbc.queryForList(SELECT_COLUMNS + " WHERE id = ?", id);
        if (rows.isEmpty()) throw new IllegalArgumentException("That investment could not be found.");
        return InvestmentFactory.fromRow(rows.get(0));
    }
}
