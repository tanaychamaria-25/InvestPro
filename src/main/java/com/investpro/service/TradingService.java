package com.investpro.service;

import com.investpro.exception.InsufficientFundsException;
import com.investpro.exception.InvalidQuantityException;
import com.investpro.model.Investment;
import com.investpro.model.TradeResult;
import com.investpro.repository.InvestmentRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

/** Executes each trade as one database transaction, keeping cash, holdings and history consistent. */
@Service
public class TradingService {
    private final JdbcTemplate jdbc;
    private final InvestmentRepository investments;
    private final TransactionTemplate transactionTemplate;

    public TradingService(JdbcTemplate jdbc, InvestmentRepository investments,
                          TransactionTemplate transactionTemplate) {
        this.jdbc = jdbc;
        this.investments = investments;
        this.transactionTemplate = transactionTemplate;
    }

    public TradeResult trade(long userId, long investmentId, String requestedAction, int quantity) {
        String action = requestedAction == null ? "" : requestedAction.trim().toUpperCase(Locale.ROOT);
        if (!action.equals("BUY") && !action.equals("SELL")) {
            throw new IllegalArgumentException("Action must be BUY or SELL.");
        }
        if (quantity <= 0 || quantity > 1_000_000) {
            throw new InvalidQuantityException("Enter a quantity between 1 and 1,000,000.");
        }
        TradeResult result = transactionTemplate.execute(status -> executeTrade(userId, investmentId, action, quantity));
        if (result == null) throw new IllegalStateException("The trade did not return a result.");
        return result;
    }

    /** Overloaded convenience method used to demonstrate method overloading in the OOP design. */
    public TradeResult buy(long userId, long investmentId, int quantity) {
        return trade(userId, investmentId, "BUY", quantity);
    }

    /** Overloaded convenience method accepts a model object instead of an ID. */
    public TradeResult buy(long userId, Investment investment, int quantity) {
        return buy(userId, investment.getId(), quantity);
    }

    public TradeResult sell(long userId, long investmentId, int quantity) {
        return trade(userId, investmentId, "SELL", quantity);
    }

    public TradeResult sell(long userId, Investment investment, int quantity) {
        return sell(userId, investment.getId(), quantity);
    }

    private TradeResult executeTrade(long userId, long investmentId, String action, int quantity) {
        List<BigDecimal> balances = jdbc.query(
                "SELECT cash_balance FROM users WHERE id = ? FOR UPDATE",
                (rs, rowNum) -> rs.getBigDecimal("cash_balance"), userId);
        if (balances.isEmpty()) throw new IllegalArgumentException("Your account could not be found. Please sign in again.");
        BigDecimal currentBalance = balances.get(0);
        Investment investment = investments.findById(investmentId);
        BigDecimal total = investment.calculateTotalCost(quantity).setScale(2, RoundingMode.HALF_UP);

        List<Map<String, Object>> existing = jdbc.queryForList(
                "SELECT quantity, average_cost FROM holdings WHERE user_id = ? AND investment_id = ? FOR UPDATE",
                userId, investmentId);

        if (action.equals("BUY")) {
            if (currentBalance.compareTo(total) < 0) {
                throw new InsufficientFundsException("Not enough cash for this purchase. Reduce the quantity or sell an investment first.");
            }
            jdbc.update("UPDATE users SET cash_balance = cash_balance - ? WHERE id = ?", total, userId);
            if (existing.isEmpty()) {
                jdbc.update("INSERT INTO holdings (user_id, investment_id, quantity, average_cost) VALUES (?, ?, ?, ?)",
                        userId, investmentId, quantity, investment.getPrice());
            } else {
                int oldQuantity = ((Number) existing.get(0).get("quantity")).intValue();
                BigDecimal oldAverage = (BigDecimal) existing.get(0).get("average_cost");
                int newQuantity = oldQuantity + quantity;
                BigDecimal newAverage = oldAverage.multiply(BigDecimal.valueOf(oldQuantity))
                        .add(investment.getPrice().multiply(BigDecimal.valueOf(quantity)))
                        .divide(BigDecimal.valueOf(newQuantity), 4, RoundingMode.HALF_UP);
                jdbc.update("UPDATE holdings SET quantity = ?, average_cost = ? WHERE user_id = ? AND investment_id = ?",
                        newQuantity, newAverage, userId, investmentId);
            }
        } else {
            if (existing.isEmpty()) throw new InvalidQuantityException("You do not own this investment yet.");
            int heldQuantity = ((Number) existing.get(0).get("quantity")).intValue();
            if (quantity > heldQuantity) {
                throw new InvalidQuantityException("You only own " + heldQuantity + " unit(s) of " + investment.getSymbol() + ".");
            }
            jdbc.update("UPDATE users SET cash_balance = cash_balance + ? WHERE id = ?", total, userId);
            if (quantity == heldQuantity) {
                jdbc.update("DELETE FROM holdings WHERE user_id = ? AND investment_id = ?", userId, investmentId);
            } else {
                jdbc.update("UPDATE holdings SET quantity = quantity - ? WHERE user_id = ? AND investment_id = ?",
                        quantity, userId, investmentId);
            }
        }

        jdbc.update("INSERT INTO transactions (user_id, investment_id, transaction_type, quantity, price, total_amount) VALUES (?, ?, ?, ?, ?, ?)",
                userId, investmentId, action, quantity, investment.getPrice(), total);
        BigDecimal newBalance = jdbc.queryForObject("SELECT cash_balance FROM users WHERE id = ?", BigDecimal.class, userId);
        String message = action.equals("BUY") ? "Purchase completed." : "Sale completed.";
        return new TradeResult(action, investment.getSymbol(), quantity, total, newBalance, message);
    }
}
