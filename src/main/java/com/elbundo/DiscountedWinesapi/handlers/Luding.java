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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class Luding implements Handler {
    private final String site = "https://luding.ru";
    @Override
    public List<Wine> getAllWines() {
        if(true) return new ArrayList<>();
        List<Wine> result = new ArrayList<>();
        String address = "https://luding.ru/collection/wine/filter/sale_mark_s1-is-3661e4d671d094d2cfbe06e331ebfe65/country-is-russia/apply/";
        int page = 1;
        while(true) {
            HttpGet request = new HttpGet(address + (page==1 ? "" : ("?PAGEN_1=" + page)));
            request.addHeader("Host", "luding.ru");
            request.addHeader("Accept-Encoding", "gzip, deflate, br");
            request.addHeader("Connection", "keep-alive");
            request.addHeader("Cookie", "AORS_FIRST_CLICK=%7B%22UTM_Source%22%3A%22type-in%22%2C%22UTM_Medium%22%3A%22direct%22%2C%22UTM_Campaign%22%3A%22%22%2C%22date%22%3A%222022-08-05%2023%3A28%3A20%22%2C%22HTTP_Referer%22%3A%22%22%7D; AORS_LAST_CLICK=%7B%22UTM_Source%22%3A%22type-in%22%2C%22UTM_Medium%22%3A%22direct%22%2C%22UTM_Campaign%22%3A%22%22%2C%22date%22%3A%222022-08-18%2017%3A20%3A08%22%2C%22HTTP_Referer%22%3A%22%22%7D; BITRIX_SM_SALE_UID=152188371; BITRIX_CONVERSION_CONTEXT_s1=%7B%22ID%22%3A67%2C%22EXPIRE%22%3A1660856340%2C%22UNIQUE%22%3A%5B%22conversion_visit_day%22%5D%7D; rrpvid=3460039053903; popmechanic_sbjs_migrations=popmechanic_1418474375998%3D1%7C%7C%7C1471519752600%3D1%7C%7C%7C1471519752605%3D1; PHPSESSID=42g9eoh736edrchpfb378qc4ip; request_price_timer=120; request_price_value=; request_price_type=");
            request.addHeader("Upgrade-Insecure-Requests", "1");
            request.addHeader("Sec-Fetch-Dest", "document");
            request.addHeader("Sec-Fetch-Mode", "navigate");
            request.addHeader("Sec-Fetch-Site", "none");
            request.addHeader("Sec-Fetch-User", "?1");
            request.addHeader("TE", "trailers");
            request.addHeader("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:103.0) Gecko/20100101 Firefox/103.0");
            request.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8");
            request.addHeader("Accept-Language", "en-US,en;q=0.5");
            try (CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(RequestConfig.custom().setRedirectsEnabled(false).build()).build();
                 CloseableHttpResponse response = httpClient.execute(request)) {
                log.info(site + ", page = " + page + "; " + response.getStatusLine().getStatusCode());
                if(response.getStatusLine().getStatusCode() != 200)
                    break;
                String entity = EntityUtils.toString(response.getEntity());
                Document doc = Jsoup.parse(entity);
                Elements items = doc.getElementsByClass("card-component--vertical");
                for (Element item : items) {
                    try{
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
                        if(wine.getPrice() < DiscountedWinesApiApplication.MinPrice)
                            continue;
                        wine.setPriceWithDiscount(Double.parseDouble(item.getElementsByClass("price__discount").get(0).text().replace(" ", "").replace("Р", "")));
                        result.add(wine);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
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
