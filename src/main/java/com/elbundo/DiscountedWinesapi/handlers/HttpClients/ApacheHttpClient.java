package com.elbundo.DiscountedWinesapi.handlers.HttpClients;

import com.elbundo.DiscountedWinesapi.handlers.Exception.ForbiddenException;
import com.elbundo.DiscountedWinesapi.handlers.Exception.NotFoundException;
import com.elbundo.DiscountedWinesapi.handlers.Exception.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
@Slf4j
public class ApacheHttpClient implements HttpClient {
    CloseableHttpClient httpClient;
    HttpGet request = new HttpGet();
    String fileNameHeaders;

    public ApacheHttpClient(String filePath) throws IOException {
        fileNameHeaders = filePath;
        httpClient = HttpClients.custom()
                .setDefaultRequestConfig(
                        RequestConfig.custom()
                                .setSocketTimeout(5000)
                                .setConnectTimeout(5000)
                                .setRedirectsEnabled(false)
                                .build())
                .build();
        configureRequest();
    }

    private void configureRequest() throws IOException {
        String path = System.getenv("PATHTOHEADERSFILES");
        try (BufferedReader reader = new BufferedReader(new FileReader((path != null ? path : "") + "/" + fileNameHeaders))) {
            String line;
            while ((line = reader.readLine()) != null) {
                request.addHeader(line.trim(), reader.readLine().trim());
            }
        }
    }

    public String get(String url) throws Exception {
        try {
            request.setURI(new URI(url));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        try(CloseableHttpResponse response = httpClient.execute(request)) {
            switch (response.getStatusLine().getStatusCode()) {
                case 200:
                    log.info("{} : 200", url);
                    break;
                case 404:
                    throw new NotFoundException("Not Found 404");
                case 403:
                    throw new ForbiddenException("Forbidden 403");
                case 401:
                    throw new UnauthorizedException("Unauthorized 401");
                default:
                    throw new RuntimeException("" + response.getStatusLine().getReasonPhrase() + " " + response.getStatusLine().getStatusCode());
            }
            return EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws IOException {
        httpClient.close();
    }
}