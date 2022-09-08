package com.elbundo.DiscountedWinesapi.handlers;


import com.elbundo.DiscountedWinesapi.model.Wine;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class AmWine implements Handler{
    private final String SITE = "https://amwine.ru";
    private final String PRODUCTS = "window.products = ";
    @Override
    public List<Wine> getAllWines() {
        String address = "https://amwine.ru/catalog/vino/filter/country-is-rossiya/value-is-0.75/";
        List<Wine> list = new ArrayList<>();
        CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(5000)
                        .setSocketTimeout(30000)
                        .setRedirectsEnabled(true)
                        .build())
                .build();

        for(int page = 1; page < 20; page++) {
            HttpGet httpGet = new HttpGet(address + "?page=" + page);
            httpGet.setHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:102.0) Gecko/20100101 Firefox/102.0");
            httpGet.setHeader(HttpHeaders.COOKIE, "AMWINE__CITY_NAME=%D0%9C%D0%BE%D1%81%D0%BA%D0%B2%D0%B0; AMWINE__CITY_SALE_LOCATION_ID=19; AMWINE__REGION_ELEMENT_ID=342; AMWINE__REGION_ELEMENT_XML_ID=77; AMWINE__REGION_CODE=moscow; AMWINE__AUTO_GEOSERVICE=1; AMWINE__AB_PRIMARY_KEY=1; PHPSESSID=YHRJQtYXLr5WYr32Ys5odfyeAzXet8EE; AMWINE__GUEST_ID=117414675; AMWINE__LAST_VISIT=08.08.2022%2016%3A43%3A10; popmechanic_sbjs_migrations=popmechanic_1418474375998%3D1%7C%7C%7C1471519752600%3D1%7C%7C%7C1471519752605%3D1; _userGUID=0:l6gxga2y:wFbQeSL1ljn_J3U5ooJU5MTOxIC0r4HC; AMWINE__IS_ADULT=Y");
            CloseableHttpResponse httpResponse;
            InputStream stream = null;
            try {
                httpResponse = httpClient.execute(httpGet);
                log.info(SITE + "page " + page + "; " + httpResponse.getStatusLine().getStatusCode());
                if (httpResponse.getStatusLine().getStatusCode() != 200) {
                    break;
                }
                stream = httpResponse.getEntity().getContent();
            } catch (IOException e) {
                //Сделать что-нибудь
            }
            try (BufferedReader in = new BufferedReader(new InputStreamReader(stream))) {
                String line;
                while ((line = in.readLine()) != null) {
                    if(line.contains(PRODUCTS)) {
                        line = line.replaceAll(PRODUCTS, "");
                        break;
                    }
                }
                JSONArray jsonArray = new JSONArray(line);
                for(int i = 0; i < jsonArray.length(); i++){
                    JSONObject wine_card = (JSONObject) jsonArray.get(i);
                    if(!wine_card.getString("sale").equals("")) {
                        Wine wine = new Wine();
                        wine.setSite(SITE);
                        wine.setAlias("");
                        wine.setTitle(wine_card.getString("name"));
                        wine.setPage(SITE + wine_card.getString("link"));
                        wine.setPathImage(SITE + wine_card.getString("preview_picture"));
                        wine.setPrice(Double.parseDouble(wine_card.getString("price")));
                        wine.setPriceWithDiscount(Double.parseDouble(wine_card.getString("old_price")));
                        wine.setDiscount(Math.abs(Double.parseDouble(wine_card.getString("sale").replaceAll("%", ""))));
                        wine.setRatings("");
                        if (wine.getPrice() < 400)
                            continue;
                        list.add(wine);
                    }
                }
            } catch (Exception ignored) {

            }
        }
        return list;
    }

    @Override
    public String getSite() {
        return SITE;
    }
}
