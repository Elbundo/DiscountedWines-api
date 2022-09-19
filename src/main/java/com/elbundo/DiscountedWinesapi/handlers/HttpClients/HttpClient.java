package com.elbundo.DiscountedWinesapi.handlers.HttpClients;

public interface HttpClient extends AutoCloseable {
    String get(String url) throws Exception;
}
