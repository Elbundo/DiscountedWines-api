package com.elbundo.DiscountedWinesapi.handlers.Parsers;

import com.elbundo.DiscountedWinesapi.handlers.Exception.LowPriceException;
import com.elbundo.DiscountedWinesapi.model.Wine;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractHttpParser extends AbstractParser {
    @NotNull
    @Override
    public List<Wine> parsePage(String page) {
        List<Wine> result = new ArrayList<>();
        Document doc = Jsoup.parse(page);
        Elements items = parseCatalog(doc);
        for (Element item : items) {
            try{
                result.add(parseWine(item));
            } catch (Exception ignored) {

            }
        }

        return result;
    }
    @NotNull
    protected abstract Elements parseCatalog(Document doc);
    @NotNull
    protected abstract Wine parseWine(Element item) throws LowPriceException;
}
