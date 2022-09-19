package com.elbundo.DiscountedWinesapi.handlers.Exception;

public class NotFoundException extends Exception {
    public NotFoundException(String msg) {
        super(msg);
    }

    @Override
    public String toString() {
        return "Not Found 404";
    }
}
