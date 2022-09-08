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
public class SimpleWine implements Handler{
    private final String site = "https://simplewine.ru";
    @Override
    public List<Wine> getAllWines() {
        List<Wine> result = new ArrayList<>();
        String address = "https://simplewine.ru/stock/discount/filter/country-rossiya/volume-0_75/";
        int page = 1;
        while(true){
            try {
                Document doc = Jsoup.connect(address + "page" + page).get();
                log.info(site + ";page = " + page + "; 200");
                Elements items = doc.getElementsByClass("product-snippet");
                for(Element item : items) {
                    try {
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
                            continue;
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
                        result.add(wine);
                    } catch(Exception e) {

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
