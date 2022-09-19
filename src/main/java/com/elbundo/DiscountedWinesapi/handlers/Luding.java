package com.elbundo.DiscountedWinesapi.handlers;

import com.elbundo.DiscountedWinesapi.DiscountedWinesApiApplication;
import com.elbundo.DiscountedWinesapi.handlers.Exception.LowPriceException;
import com.elbundo.DiscountedWinesapi.handlers.HttpClients.ApacheHttpClient;
import com.elbundo.DiscountedWinesapi.handlers.HttpClients.HttpClient;
import com.elbundo.DiscountedWinesapi.handlers.Parsers.AbstractHttpParser;
import com.elbundo.DiscountedWinesapi.model.Wine;
import org.jetbrains.annotations.NotNull;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class Luding extends AbstractHttpParser {
    @NotNull
    @Override
    protected Elements parseCatalog(Document doc) {
        return doc.getElementsByClass("card-component--vertical");
    }
    @NotNull
    @Override
    protected Wine parseWine(Element item) throws LowPriceException {
        String site = getSiteUrl();
        Wine wine = new Wine();
        wine.setSite(site);
        wine.setAlias("");
        Elements discount = item.getElementsByClass("sticker--discount").get(0).getElementsByTag("span");
        wine.setDiscount(Double.parseDouble(discount.get(1).text().replace("-", "").replace("%", "")));
        wine.setRatings("Осталось " + discount.get(2).text());
        Element image = item.getElementsByClass("card-component__img").get(0);
        wine.setPage(site + image.getElementsByTag("a").get(0).attr("href"));
        wine.setPathImage(site + image.getElementsByTag("img").attr("src").replace("600_600_1", "800_900_0").replace("webp", "png"));
        wine.setTitle(image.getElementsByTag("img").attr("alt"));
        wine.setPrice(Double.parseDouble(item.getElementsByClass("price__main").get(0).text().replace(" ", "").replace("Р", "")));
        wine.setPriceWithDiscount(Double.parseDouble(item.getElementsByClass("price__discount").get(0).text().replace(" ", "").replace("Р", "")));
        if(wine.getPrice() < DiscountedWinesApiApplication.MIN_PRICE)
            throw new LowPriceException();
        return wine;
    }

    @NotNull
    @Override
    protected String getPageFilter(int i) {
        return i==1 ? "" : ("?PAGEN_1=" + i);
    }

    @NotNull
    @Override
    protected String getPagePath() {
        return "/collection/wine/filter/sale_mark_s1-is-3661e4d671d094d2cfbe06e331ebfe65/country-is-russia/apply/";
    }

    @NotNull
    @Override
    public String getSiteUrl() {
        return "https://luding.ru";
    }

    @Override
    protected int getPageCount(String page) {
        return 1000;
    }

    @NotNull
    @Override
    protected HttpClient getHttpClient() throws IOException {
        return new ApacheHttpClient("ludingheaders.txt");
    }
}
