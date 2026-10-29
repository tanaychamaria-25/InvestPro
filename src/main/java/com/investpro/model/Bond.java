package com.investpro.model;

import java.math.BigDecimal;

public final class Bond extends Investment {
    public Bond(long id, String symbol, String name, BigDecimal price, String description) {
        super(id, symbol, name, price, description);
    }

    @Override public String getType() { return "BOND"; }
    @Override public String getRiskLevel() { return "Low–Medium"; }
    @Override public String getInvestorNote() { return "Illustrative fixed-income product; not an actual bond offer."; }
}
