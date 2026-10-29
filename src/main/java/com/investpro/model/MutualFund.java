package com.investpro.model;

import java.math.BigDecimal;

public final class MutualFund extends Investment {
    public MutualFund(long id, String symbol, String name, BigDecimal price, String description) {
        super(id, symbol, name, price, description);
    }

    @Override public String getType() { return "MUTUAL_FUND"; }
    @Override public String getRiskLevel() { return "Medium"; }
    @Override public String getInvestorNote() { return "Illustrative fund units; values are simulated and not live NAVs."; }
}
