package com.investpro.model;

import java.math.BigDecimal;

public record TradeResult(String action, String symbol, int quantity, BigDecimal totalAmount,
                          BigDecimal cashBalance, String message) { }
