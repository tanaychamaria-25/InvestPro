package com.investpro.model;

import java.math.BigDecimal;

/** User profile deliberately excludes the stored password hash. */
public final class User {
    private final long id;
    private final String fullName;
    private final String email;
    private final BigDecimal cashBalance;

    public User(long id, String fullName, String email, BigDecimal cashBalance) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.cashBalance = cashBalance;
    }

    public long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public BigDecimal getCashBalance() { return cashBalance; }
}
