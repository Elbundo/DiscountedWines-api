package com.elbundo.DiscountedWinesapi.handlers;

import com.elbundo.DiscountedWinesapi.DiscountedWinesApiApplication;
import com.elbundo.DiscountedWinesapi.model.Wine;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class WineHelp implements Handler{
    private final String site = "https://winehelp2.ru";
    @Override
    public List<Wine> getAllWines() {
        List<Wine> result = new ArrayList<>();
        String address = "https://winehelp2.ru/aktsii-rossii/rz_available-is-available/volume-is-0-75l/drink_type-is-beloe-wino-or-wine-or-a%20sparkling%20wine-or-krasnoe-wino-or-rosovoe-wino/";
        int page = 1;
        int maxPage = 1000;
        while(page <= maxPage){
            try {
                Document doc = Jsoup.connect(address + "?PAGEN_1=" + page).get();
                Elements pages = doc.getElementsByClass("bx-pagination-container").get(0).getElementsByTag("li");
                if(maxPage == 1000) {
                    maxPage = Integer.parseInt(pages.get(pages.size() - 2).getElementsByTag("span").get(0).text());
                }
                Elements items = doc.getElementsByClass("product-item-container");
                for(Element item : items) {
                    try {
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
                            continue;
                        Element priceWithDiscount = item.getElementsByClass("product-item-price-current").get(0);
                        wine.setPriceWithDiscount(Double.parseDouble(priceWithDiscount.text().replace("руб.", "").replaceAll(" ", "")));
                        result.add(wine);
                    } catch(Exception ignored) {

                    }
                }
            } catch (IOException e) {
                break;
            }
            page++;
        }
        return result;
    }

    @Override
    public String getSite() {
        return site;
    }
}
