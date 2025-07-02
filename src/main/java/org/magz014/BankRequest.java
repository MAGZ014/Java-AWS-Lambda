package org.magz014;

import java.math.BigDecimal;

public class BankRequest {

    // BigDecimal nos ayuda a terner calculos precisos
    private BigDecimal amount;
    private Integer term;
    private BigDecimal rate;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Integer getTerm() {
        return term;
    }

    public void setTerm(Integer term) {
        this.term = term;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }
}
