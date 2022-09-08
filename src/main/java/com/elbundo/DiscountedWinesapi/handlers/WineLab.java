package com.elbundo.DiscountedWinesapi.handlers;

import com.elbundo.DiscountedWinesapi.DiscountedWinesApiApplication;
import com.elbundo.DiscountedWinesapi.model.Wine;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URI;
import java.util.*;

@Slf4j
@Component
public class WineLab implements Handler{
    private final String site = "https://winelab.ru";

    private final String fileHeaders = System.getenv("WINELABHEADERS");

    @Override
    public List<Wine> getAllWines() {
        List<Wine> result = new ArrayList<>();
        String address = "https://www.winelab.ru/catalog/vino?q=:relevance:priceTagColor:GOLD:priceTagColor:PURPLE:Capacity:0.75:countryfiltr:%D0%A0%D0%BE%D1%81%D1%81%D0%B8%D1%8F&text=&sort=relevance";
        Map<String, String> cookies = new HashMap<>();
        Map<String, String> headers = new HashMap<>();
        HttpGet request = new HttpGet();
        System.out.println(fileHeaders);
        try (BufferedReader reader = new BufferedReader(new FileReader(fileHeaders))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                request.addHeader(line, reader.readLine().trim());
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        int page = 1;
        while (true) {
            try {
                request.setURI(new URI(address + "&page=" + page));
                CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(RequestConfig.custom().setRedirectsEnabled(true).build()).build();
                CloseableHttpResponse response = httpClient.execute(request);
                log.info(site + "; page = " + page + "; " + response.getStatusLine().getStatusCode());
                if(response.getStatusLine().getStatusCode() != 200)
                    break;
                Document doc = Jsoup.parse(EntityUtils.toString(response.getEntity()));
                Elements items = doc.getElementsByClass("product_card_wr");
                for(Element item : items){
                    try{
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
                        request.setURI(new URI(wine.getPage()));
                        CloseableHttpResponse responseWinePage = httpClient.execute(request);
                        log.info(wine.getPage() + "; " + responseWinePage.getStatusLine().getStatusCode());
                        if(responseWinePage.getStatusLine().getStatusCode() != 200)
                            continue;
                        Document winePage = Jsoup.parse(EntityUtils.toString(responseWinePage.getEntity()));
                        Element image = winePage.getElementsByClass("js-carousel-zoom").get(0).getElementsByClass("owl-lazy").get(0);
                        wine.setPathImage(site + image.attr("data-src"));
                        Element price = item.getElementsByClass("discount__value").get(0);
                        wine.setPrice(Double.parseDouble(price.attr("data-price")));
                        if(wine.getPrice() < DiscountedWinesApiApplication.MIN_PRICE)
                            continue;
                        Element priceWithDiscount = item.getElementsByClass("tooltip").get(0);
                        wine.setPriceWithDiscount(Double.parseDouble(priceWithDiscount.attr("data-price")));
                        wine.setDiscount(100 * (1 - wine.getPriceWithDiscount()/wine.getPrice()));
                        wine.setRatings("");
                        result.add(wine);
                    }   catch (Exception e) {
                    }
                }
                httpClient.close();
                response.close();
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
