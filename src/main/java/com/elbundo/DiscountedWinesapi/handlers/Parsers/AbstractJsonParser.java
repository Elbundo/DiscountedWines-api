package com.elbundo.DiscountedWinesapi.handlers.Parsers;

import com.elbundo.DiscountedWinesapi.handlers.Exception.LowPriceException;
import com.elbundo.DiscountedWinesapi.model.Wine;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractJsonParser extends AbstractParser {
    @NotNull
    @Override
    public List<Wine> parsePage(String page) {
        List<Wine> result = new ArrayList<>();
        JSONArray items;
        try {
            items = parseCatalog(page);
        } catch (Exception e) {
            return result;
        }
        for (int i = 0; i < items.length(); i++) {
            try{
                JSONObject item = (JSONObject) items.get(i);
                result.add(parseWine(item));
            } catch (Exception ignored) {

            }
        }
        return result;
    }
    @NotNull
    protected abstract JSONArray parseCatalog(String doc) throws JSONException;
    @NotNull
    protected abstract Wine parseWine(JSONObject item) throws LowPriceException;
}
