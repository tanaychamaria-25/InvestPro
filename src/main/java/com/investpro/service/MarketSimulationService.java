package com.investpro.service;

import com.investpro.model.MarketMovementResult;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

/** Applies a user-triggered, illustrative price movement to the sample marketplace. */
@Service
public class MarketSimulationService {
    private final JdbcTemplate jdbc;
    private final TransactionTemplate transactions;

    public MarketSimulationService(JdbcTemplate jdbc, TransactionTemplate transactions) {
        this.jdbc = jdbc;
        this.transactions = transactions;
    }

    public MarketMovementResult simulateMovement() {
        Integer count = transactions.execute(status -> {
            List<Long> ids = jdbc.query("SELECT id FROM investments ORDER BY id", (rs, rowNum) -> rs.getLong("id"));
            int updated = 0;
            for (Long id : ids) {
                // A small deterministic-per-asset draw for this click; this is deliberately not live data.
                double percent = ThreadLocalRandom.current().nextDouble(-0.05, 0.050001);
                BigDecimal multiplier = BigDecimal.valueOf(1.0 + percent).setScale(6, RoundingMode.HALF_UP);
                jdbc.update("UPDATE investments SET price = GREATEST(0.01, ROUND(price * ?, 2)) WHERE id = ?", multiplier, id);
                updated++;
            }
            return updated;
        });
        // Percent is not presented as a financial performance calculation; only report the count of quotes changed.
        int assets = count == null ? 0 : count;
        return new MarketMovementResult(assets, "Illustrative market movement simulated for " + assets + " asset(s).");
    }
}
