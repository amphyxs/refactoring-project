package com.par.parapp.dto;

public class BalanceResponse {
    private Double balance;
    private Double bonuses;

    public Double getBonuses() {
        return bonuses;
    }

    public void setBonuses(Double bonuses) {
        this.bonuses = bonuses;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public BalanceResponse(Double balance, Double bonuses) {
        this.balance = balance;
        this.bonuses = bonuses;
    }
}
