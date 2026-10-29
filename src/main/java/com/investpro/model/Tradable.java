package com.investpro.model;

import com.investpro.exception.InvalidQuantityException;
import java.math.BigDecimal;

/** Contract shared by investment products that can be simulated in the marketplace. */
public interface Tradable {
    BigDecimal calculateTotalCost(int quantity) throws InvalidQuantityException;
}
