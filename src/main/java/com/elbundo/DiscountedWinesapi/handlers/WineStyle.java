package com.elbundo.DiscountedWinesapi.handlers;

import com.elbundo.DiscountedWinesapi.DiscountedWinesApiApplication;
import com.elbundo.DiscountedWinesapi.model.Wine;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class WineStyle implements Handler{
    private final String site = "https://winestyle.ru";
    @Override
    public List<Wine> getAllWines() {
        List<Wine> result = new ArrayList<>();
        String address = "https://winestyle.ru/promo/wine/russia/750ml/discount_ll/";
        HtmlPage htmlPage;
        int page = 1;
        while (true) {
            try(WebClient webClient = new WebClient()) {
                webClient.getOptions().setJavaScriptEnabled(false);
                webClient.getOptions().setThrowExceptionOnScriptError(false);
                webClient.getCookieManager().setCookiesEnabled(true);
                htmlPage = webClient.getPage(address + "?page=" + page);
                Document doc = Jsoup.parse(htmlPage.getWebResponse().getContentAsString());
                Elements items = doc.getElementsByClass("item-block");
                for(Element item : items){
                    try{
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
                            continue;
                        wine.setPriceWithDiscount(Double.parseDouble(item.getElementsByClass("price-old").get(0).text().replace(" ", "").replace("руб.", "")));
                        result.add(wine);
                    } catch(Exception e) {
                    }
                }
            }catch (MalformedURLException e) {
                log.error(site + ": Can't get the page!");
                return result;
            } catch (IOException e) {
                log.error(site + ": can't get the page");
                return result;
            } catch (Exception e) {
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
