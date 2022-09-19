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

public class SimpleWine extends AbstractHttpParser {
    @NotNull
    @Override
    protected Elements parseCatalog(Document doc) {
        return doc.getElementsByClass("product-snippet");
    }

    @NotNull
    @Override
    protected Wine parseWine(Element item) throws LowPriceException {
        String site = getSiteUrl();
        Wine wine = new Wine();
        wine.setSite(site);
        Element name = item.getElementsByClass("product-snippet__name").get(0);
        String img = item.getElementsByClass("product-card__image").get(0).attr("src");
        wine.setPathImage(img.substring(0, img.indexOf("@")));
        wine.setPage(site + name.attr("href"));
        wine.setTitle(name.text());
        Elements subTitle = item.getElementsByClass("product-snippet__desc").get(0).getElementsByTag("a");
        StringBuilder alias = new StringBuilder();
        for(Element elem : subTitle) {
            alias.append(elem.text()).append(", ");
        }
        wine.setAlias(alias.toString());
        Element priceAndDiscount = item.getElementsByClass("product-snippet__discount").get(0);
        wine.setPrice(Double.parseDouble(priceAndDiscount.getAllElements().get(1).text().replace("₽", "").replaceAll(" ", "")));
        if(wine.getPrice() < DiscountedWinesApiApplication.MIN_PRICE)
            throw new LowPriceException();
        wine.setDiscount(Double.parseDouble(priceAndDiscount.getAllElements().get(2).text().replace("%", "").replace("-", "")));
        Element priceWithDiscount = item.getElementsByClass("product-snippet__price").get(0);
        wine.setPriceWithDiscount(Double.parseDouble(priceWithDiscount.text().replace("₽", "").replaceAll(" ", "")));
        Elements ratings = item.getElementsByClass("product-snippet__ratings-item");
        StringBuilder wineRating = new StringBuilder();
        for(Element rating : ratings) {
            Elements rate = rating.getElementsByTag("span");
            if(rate.size() == 2) {
                wineRating.append(rate.get(0).text()).append(" ");
                wineRating.append(rate.get(1).text()).append("\n");
            }
        }
        wine.setRatings(wineRating.toString());
        return wine;
    }

    @NotNull
    @Override
    protected String getPageFilter(int i) {
        return i == 1 ? "" : ("page" + i);
    }

    @NotNull
    @Override
    protected String getPagePath() {
        return "/stock/discount/filter/country-rossiya/volume-0_75/";
    }

    @NotNull
    @Override
    public String getSiteUrl() {
        return "https://simplewine.ru";
    }

    @Override
    protected int getPageCount(String page) {
        return 1000;
    }

    @NotNull
    @Override
    protected HttpClient getHttpClient() throws IOException {
        return new ApacheHttpClient("simplewineheaders.txt");
    }
}
