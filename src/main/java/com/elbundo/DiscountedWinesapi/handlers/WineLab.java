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

import java.io.IOException;

public class WineLab extends AbstractHttpParser {
    @NotNull
    @Override
    protected Elements parseCatalog(Document doc) {
        return doc.getElementsByClass("product_card_wr");
    }

    @NotNull
    @Override
    protected Wine parseWine(Element item) throws LowPriceException {
        String site = getSiteUrl();
        Wine wine = new Wine();
        wine.setSite(site);
        Elements aliases = item.getElementsByClass("description").get(0).getElementsByTag("span");
        StringBuilder subTitle = new StringBuilder();
        for(Element alias : aliases) {
            if(alias.hasClass("description__line"))
                continue;
            subTitle.append(alias.text()).append(" ");
        }
        wine.setAlias(subTitle.toString());
        Element title = item.getElementsByClass("item_name").get(0);
        wine.setTitle(title.text());
        wine.setPage(site + title.attr("data-href"));
        Document winePage;
        try (HttpClient httpClient = getHttpClient()){
            winePage = Jsoup.parse(httpClient.get(wine.getPage()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Element image = winePage.getElementsByClass("js-carousel-zoom").get(0).getElementsByClass("owl-lazy").get(0);
        wine.setPathImage(site + image.attr("data-src"));
        Element price = item.getElementsByClass("discount__value").get(0);
        wine.setPrice(Double.parseDouble(price.attr("data-price")));
        if(wine.getPrice() < DiscountedWinesApiApplication.MIN_PRICE)
            throw new LowPriceException();
        Element priceWithDiscount = item.getElementsByClass("tooltip").get(0);
        wine.setPriceWithDiscount(Double.parseDouble(priceWithDiscount.attr("data-price")));
        wine.setDiscount(100 * (1 - wine.getPriceWithDiscount()/wine.getPrice()));
        wine.setRatings("");
        return wine;
    }

    @NotNull
    @Override
    protected String getPageFilter(int i) {
        return i == 1 ? "" : ("&page=" + i);
    }

    @NotNull
    @Override
    protected String getPagePath() {
        return "/catalog/vino?q=:relevance:priceTagColor:GOLD:priceTagColor:PURPLE:Capacity:0.75:countryfiltr:%D0%A0%D0%BE%D1%81%D1%81%D0%B8%D1%8F&text=&sort=relevance";
    }

    @NotNull
    @Override
    public String getSiteUrl() {
        return "https://www.winelab.ru";
    }

    @Override
    protected int getPageCount(String page) {
        return 1000;
    }

    @NotNull
    @Override
    protected HttpClient getHttpClient() throws IOException {
        return new ApacheHttpClient("winelabheaders.txt");
    }
}
