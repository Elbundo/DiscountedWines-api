package com.elbundo.DiscountedWinesapi.handlers.Exception;

public class UnauthorizedException extends Exception {
    public UnauthorizedException(String msg) {
        super(msg);
    }
    @Override
    public String toString() {
        return "Unauthorized 401";
    }
}
