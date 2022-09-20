package com.elbundo.DiscountedWinesapi.handlers.Parsers;

import com.elbundo.DiscountedWinesapi.handlers.HttpClients.HttpClient;
import com.elbundo.DiscountedWinesapi.model.Wine;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public abstract class AbstractParser {
    public List<Wine> getAllWines() {
        List<Wine> result = new ArrayList<>();
        try (HttpClient httpClient = getHttpClient()) {
            int maxPage = getPageCount(httpClient.get(getPageUrl(1)));
            for(int page = 1; page <= maxPage; page++) {
                try{
                    result.addAll(parsePage(httpClient.get(getPageUrl(page))));
                } catch (Exception e) {
                    log.info("Parse end: {}", e.getMessage());
                    break;
                }
            }
        } catch (IOException e) {
            log.info("Parse headers error: {}", e.getMessage());
        }
        catch (Exception e) {
            log.info("Http client error: {}", e.getMessage());
        }
        return result;
    }

    private String getPageUrl(int i) {
        return getSiteUrl() + getPagePath() + getPageFilter(i);
    }

    @NotNull
    protected abstract String getPageFilter(int i);
    @NotNull
    protected abstract String getPagePath();
    @NotNull
    public abstract String getSiteUrl();
    protected abstract int getPageCount(String page);
    @NotNull
    protected abstract List<Wine> parsePage(String page);
    @NotNull
    protected abstract HttpClient getHttpClient() throws IOException;
}
