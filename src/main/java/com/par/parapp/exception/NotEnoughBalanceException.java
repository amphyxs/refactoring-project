package com.par.parapp.exception;

public class NotEnoughBalanceException extends RuntimeException {

    public NotEnoughBalanceException(String message) {
        super(message);
    }
}
