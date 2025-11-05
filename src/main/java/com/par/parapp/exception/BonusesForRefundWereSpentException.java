package com.par.parapp.exception;

public class BonusesForRefundWereSpentException extends RuntimeException {

    public BonusesForRefundWereSpentException() {
        super("User has spent bonuses those must be taken back");
    }
}
