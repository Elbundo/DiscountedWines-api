package com.elbundo.DiscountedWinesapi.handlers;

import com.elbundo.DiscountedWinesapi.model.Wine;

import java.util.List;

public interface Handler {
    List<Wine> getAllWines();
    String getSite();
}
