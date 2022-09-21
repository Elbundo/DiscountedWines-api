package com.elbundo.DiscountedWinesapi.handlers;

import com.elbundo.DiscountedWinesapi.handlers.Exception.LowPriceException;
import com.elbundo.DiscountedWinesapi.handlers.HttpClients.ApacheHttpClient;
import com.elbundo.DiscountedWinesapi.handlers.HttpClients.HttpClient;
import com.elbundo.DiscountedWinesapi.handlers.Parsers.AbstractJsonParser;
import com.elbundo.DiscountedWinesapi.model.Wine;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AmWine extends AbstractJsonParser {
    @NotNull
    @Override
    protected JSONArray parseCatalog(String doc) throws JSONException {
        String product = "window.products = ";
        int start = doc.indexOf(product) + product.length();
        String json = doc.substring(start, doc.indexOf("\n", start));
        return new JSONArray(json);
    }

    @NotNull
    @Override
    protected Wine parseWine(JSONObject item) throws LowPriceException {
        String site = getSiteUrl();
        Wine wine = new Wine();
        wine.setSite(site);
        wine.setAlias("");
        wine.setTitle(item.getString("name"));
        wine.setPage(site + item.getString("link"));
        wine.setPathImage(site + item.getString("preview_picture"));
        wine.setPrice(Double.parseDouble(item.getString("old_price")));
        wine.setPriceWithDiscount(Double.parseDouble(item.getString("price")));
        wine.setDiscount(Math.abs(Double.parseDouble(item.getString("sale").replaceAll("%", ""))));
        wine.setRatings("");
        if(wine.getPrice() < 400)
            throw new LowPriceException();
        return wine;
    }

    @NotNull
    @Override
    protected String getPageFilter(int i) {
        return "?page=" + i;
    }

    @NotNull
    @Override
    protected String getPagePath() {
        return "/catalog/vino/filter/country-is-rossiya/value-is-0.75/";
    }

    @NotNull
    @Override
    public String getSiteUrl() {
        return "https://amwine.ru";
    }

    @Override
    protected int getPageCount(String page) {
        return 20;
    }

    @NotNull
    @Override
    protected HttpClient getHttpClient() throws IOException {
        return new ApacheHttpClient("amwineheaders.txt");
    }
}
