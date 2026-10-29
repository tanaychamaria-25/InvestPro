package com.investpro.model;

import java.math.BigDecimal;

public final class Stock extends Investment {
    public Stock(long id, String symbol, String name, BigDecimal price, String description) {
        super(id, symbol, name, price, description);
    }

    @Override public String getType() { return "STOCK"; }
    @Override public String getRiskLevel() { return "High"; }
    @Override public String getInvestorNote() { return "Share prices can fluctuate significantly in this simulation."; }
}
