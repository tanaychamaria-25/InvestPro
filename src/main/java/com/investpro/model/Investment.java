package com.investpro.model;

import com.investpro.exception.InvalidQuantityException;
import java.math.BigDecimal;
import java.math.RoundingMode;

/** Abstract base class demonstrating encapsulation, abstraction, constructors and polymorphism. */
public abstract class Investment implements Tradable {
    private final long id;
    private final String symbol;
    private final String name;
    private final BigDecimal price;
    private final String description;

    protected Investment(long id, String symbol, String name, BigDecimal price, String description) {
        this.id = id;
        this.symbol = symbol;
        this.name = name;
        this.price = price;
        this.description = description;
    }

    /** Overloaded constructor for products without a custom description. */
    protected Investment(long id, String symbol, String name, BigDecimal price) {
        this(id, symbol, name, price, "Simulated investment product.");
    }

    public long getId() { return id; }
    public String getSymbol() { return symbol; }
    public String getName() { return name; }
    public BigDecimal getPrice() { return price; }
    public String getDescription() { return description; }

    @Override
    public BigDecimal calculateTotalCost(int quantity) {
        if (quantity <= 0) {
            throw new InvalidQuantityException("Quantity must be at least 1.");
        }
        return price.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
    }

    public abstract String getType();
    public abstract String getRiskLevel();
    public abstract String getInvestorNote();
}
