package com.elbundo.DiscountedWinesapi.handlers;

import com.elbundo.DiscountedWinesapi.DiscountedWinesApiApplication;
import com.elbundo.DiscountedWinesapi.handlers.Exception.LowPriceException;
import com.elbundo.DiscountedWinesapi.handlers.HttpClients.ApacheHttpClient;
import com.elbundo.DiscountedWinesapi.handlers.HttpClients.HttpClient;
import com.elbundo.DiscountedWinesapi.handlers.Parsers.AbstractHttpParser;
import com.elbundo.DiscountedWinesapi.model.Wine;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class WineHelp extends AbstractHttpParser {
    @NotNull
    @Override
    protected Elements parseCatalog(Document doc) {
        return doc.getElementsByClass("product-item-container");
    }

    @NotNull
    @Override
    protected Wine parseWine(Element item) throws LowPriceException {
        String site = getSiteUrl();
        Wine wine = new Wine();
        wine.setSite(site);
        Element name = item.getElementsByClass("product-item-image-wrapper").get(0);
        wine.setPage(site + name.attr("href"));
        wine.setTitle(name.attr("title"));
        String img = name.getElementsByClass("product-item-image-original").get(0).attr("style");
        wine.setPathImage(site + img.replace("background-image: url('", "").replace("');", "").replace("resize_cache/", "").replace("230_260_1/","").trim());
        Element discount = item.getElementsByClass("product-item-numb").get(0);
        wine.setDiscount(Double.parseDouble(discount.text().replace("-", "").replace("%", "")));
        Element since = item.getElementsByClass("product-item-sizer").get(0);
        wine.setRatings("Скидка действует " + since.text());
        wine.setAlias("");
        Element oldPrice = item.getElementsByClass("product-item-price-old").get(0);
        wine.setPrice(Double.parseDouble(oldPrice.text().replace("руб.", "").replaceAll(" ", "")));
        if(wine.getPrice() < DiscountedWinesApiApplication.MIN_PRICE)
            throw new LowPriceException();
        Element priceWithDiscount = item.getElementsByClass("product-item-price-current").get(0);
        wine.setPriceWithDiscount(Double.parseDouble(priceWithDiscount.text().replace("руб.", "").replaceAll(" ", "")));
        return wine;
    }

    @NotNull
    @Override
    protected String getPageFilter(int i) {
        return "?PAGEN_1=" + i;
    }

    @NotNull
    @Override
    protected String getPagePath() {
        return "/aktsii-rossii/rz_available-is-available/volume-is-0-75l/drink_type-is-beloe-wino-or-wine-or-a%20sparkling%20wine-or-krasnoe-wino-or-rosovoe-wino/";
    }

    @NotNull
    @Override
    public String getSiteUrl() {
        return "https://winehelp2.ru";
    }

    @Override
    protected int getPageCount(String page) {
        Document doc = Jsoup.parse(page);
        Elements pages = doc.getElementsByClass("bx-pagination-container").get(0).getElementsByTag("li");
        return Integer.parseInt(pages.get(pages.size() - 2).getElementsByTag("span").get(0).text());
    }

    @NotNull
    @Override
    protected HttpClient getHttpClient() throws IOException {
        return new ApacheHttpClient("winehelpheaders.txt");
    }
}
