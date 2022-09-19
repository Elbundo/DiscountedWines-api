package com.elbundo.DiscountedWinesapi.handlers.Exception;

public class ForbiddenException extends Exception{
    public ForbiddenException(String msg) {
        super(msg);
    }

    @Override
    public String toString() {
        return "Forbidden 403";
    }
}
