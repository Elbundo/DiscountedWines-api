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

public class WineStyle extends AbstractHttpParser {
    @NotNull
    @Override
    protected Elements parseCatalog(Document doc) {
        return doc.getElementsByClass("item-block");
    }

    @NotNull
    @Override
    protected Wine parseWine(Element item) throws LowPriceException {
        String site = getSiteUrl();
        Wine wine = new Wine();
        wine.setSite(site);
        Element image = item.getElementsByClass("img-block").get(0);
        wine.setPage(site + image.attr("href"));
        wine.setDiscount(Double.parseDouble(image.getElementsByClass("discount-circle").get(0).text().replace("-", "").replace("%", "")));
        wine.setPathImage(image.getElementsByTag("noscript").get(0).getElementsByTag("img").get(0).attr("src").replace("cat", "orig"));
        String title = item.getElementsByClass("title").get(0).getElementsByTag("a").text();
        wine.setTitle(title);
        String alias = item.getElementsByClass("meta").get(1).getElementsByTag("a").text();
        wine.setAlias(alias);
        Elements ratings = item.getElementsByClass("rating-name");
        StringBuilder wineRating = new StringBuilder();
        for(Element rating : ratings) {
            Elements rate = rating.getElementsByTag("span");
            if(rate.size() == 2) {
                wineRating.append(rate.get(0).text()).append(" ");
                wineRating.append(rate.get(1).text()).append("\n");
            }
        }
        wine.setRatings(wineRating.toString());
        wine.setPrice(Double.parseDouble(item.getElementsByClass("price").get(0).text().replace(" ", "").replace("руб.", "")));
        if(wine.getPrice() < DiscountedWinesApiApplication.MIN_PRICE)
            throw new LowPriceException();
        wine.setPriceWithDiscount(Double.parseDouble(item.getElementsByClass("price-old").get(0).text().replace(" ", "").replace("руб.", "")));
        return wine;
    }

    @NotNull
    @Override
    protected String getPageFilter(int i) {
        return i == 1 ? "" : ("?page=" + i);
    }

    @NotNull
    @Override
    protected String getPagePath() {
        return "/promo/wine/russia/750ml/discount_ll/";
    }

    @NotNull
    @Override
    public String getSiteUrl() {
        return "https://winestyle.ru";
    }

    @Override
    protected int getPageCount(String page) {
        return 1000;
    }

    @NotNull
    @Override
    protected HttpClient getHttpClient() throws IOException {
        return new ApacheHttpClient("winestyleheaders.txt");
    }
}
