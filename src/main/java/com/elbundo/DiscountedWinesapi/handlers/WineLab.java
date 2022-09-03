package com.elbundo.DiscountedWinesapi.handlers;

import com.elbundo.DiscountedWinesapi.DiscountedWinesApiApplication;
import com.elbundo.DiscountedWinesapi.model.Wine;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.ScriptResult;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.*;

@Slf4j
@Component
public class WineLab implements Handler{
    private final String site = "https://winelab.ru";
    @Override
    public List<Wine> getAllWines() {
        if(true) return new ArrayList<>();
        List<Wine> result = new ArrayList<>();
        String address = "https://www.winelab.ru/catalog/vino?q=:relevance:priceTagColor:GOLD:priceTagColor:PURPLE:Capacity:0.75:countryfiltr:%D0%A0%D0%BE%D1%81%D1%81%D0%B8%D1%8F&text=&sort=relevance";
        Map<String, String> cookies = new HashMap<>();
        Map<String, String> headers = new HashMap<>();
        HttpGet request = new HttpGet();
        try (BufferedReader reader = new BufferedReader(new FileReader("/home/elbundo/yaroslav/IT/projects/DiscountedWines-api/src/main/resources/static/winelabheaders.txt"))) {
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
        System.out.println("I'm here!");
        int page = 1;
        while (true) {
            try {
                request.setURI(new URI(address + "&page=" + page));
                CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(RequestConfig.custom().setRedirectsEnabled(true).build()).build();
                CloseableHttpResponse response = httpClient.execute(request);
                System.out.println(response.getStatusLine());
                if(response.getStatusLine().getStatusCode() != 200)
                    break;
                Document doc = Jsoup.parse(EntityUtils.toString(response.getEntity()));
                log.debug(doc.text());
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
                        Element image = item.getElementsByAttributeValue("itemprop", "image").get(0);
                        wine.setPathImage(image.attr("src"));
                        Element price = item.getElementsByClass("discount__value").get(0);
                        wine.setPrice(Double.parseDouble(price.attr("data-price")));
                        if(wine.getPrice() < DiscountedWinesApiApplication.MinPrice)
                            continue;
                        Element priceWithDiscount = item.getElementsByClass("tooltip").get(0);
                        wine.setPriceWithDiscount(Double.parseDouble(priceWithDiscount.attr("data-price")));
                        wine.setDiscount(100 * (1 - wine.getPriceWithDiscount()/wine.getPrice()));
                        wine.setRatings("");
                        result.add(wine);
                    }   catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                httpClient.close();
                response.close();
            } catch (Exception e) {
                System.out.println(e.getMessage());
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
