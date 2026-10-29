package com.investpro.model;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;

/** Maps database rows to concrete investment types, enabling runtime polymorphism. */
public final class InvestmentFactory {
    private InvestmentFactory() { }

    public static Investment fromRow(Map<String, Object> row) {
        long id = ((Number) row.get("id")).longValue();
        String symbol = String.valueOf(row.get("symbol"));
        String name = String.valueOf(row.get("name"));
        String type = String.valueOf(row.get("type")).toUpperCase(Locale.ROOT);
        BigDecimal price = (BigDecimal) row.get("price");
        String description = String.valueOf(row.getOrDefault("description", "Simulated investment product."));

        return switch (type) {
            case "STOCK" -> new Stock(id, symbol, name, price, description);
            case "BOND" -> new Bond(id, symbol, name, price, description);
            case "MUTUAL_FUND" -> new MutualFund(id, symbol, name, price, description);
            default -> throw new IllegalArgumentException("Unsupported investment type: " + type);
        };
    }
}
