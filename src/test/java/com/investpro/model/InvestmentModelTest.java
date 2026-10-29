package com.investpro.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.investpro.exception.InvalidQuantityException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class InvestmentModelTest {
    @Test
    void stockCalculatesTotalCostThroughTradableContract() {
        Tradable item = new Stock(1L, "TEST", "Test Stock", new BigDecimal("125.50"), "Sample");
        assertEquals(new BigDecimal("376.50"), item.calculateTotalCost(3));
    }

    @Test
    void concreteInvestmentOverridesItsTypeAndRisk() {
        Investment stock = new Stock(1L, "TEST", "Test Stock", new BigDecimal("125.50"), "Sample");
        Investment bond = new Bond(2L, "TBOND", "Test Bond", new BigDecimal("100.00"), "Sample");
        assertEquals("STOCK", stock.getType());
        assertEquals("BOND", bond.getType());
        assertEquals("High", stock.getRiskLevel());
        assertEquals("Low–Medium", bond.getRiskLevel());
    }

    @Test
    void rejectsZeroOrNegativeQuantity() {
        Investment stock = new Stock(1L, "TEST", "Test Stock", new BigDecimal("125.50"), "Sample");
        assertThrows(InvalidQuantityException.class, () -> stock.calculateTotalCost(0));
        assertThrows(InvalidQuantityException.class, () -> stock.calculateTotalCost(-1));
    }
}
