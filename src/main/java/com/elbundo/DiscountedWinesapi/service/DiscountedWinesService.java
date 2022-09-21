package com.elbundo.DiscountedWinesapi.service;

import com.elbundo.DiscountedWinesapi.model.Wine;

import java.util.List;

public interface DiscountedWinesService {
    public List<Wine> discountedWines();
}
