package com.elbundo.DiscountedWinesapi.handlers.HttpClients;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
public class WebHttpClient implements HttpClient{
    WebClient webClient;
    public WebHttpClient() {
        webClient = new WebClient();
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setPrintContentOnFailingStatusCode(false);
        webClient.getOptions().setJavaScriptEnabled(false);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getCookieManager().setCookiesEnabled(true);
    }
    @Override
    public String get(String url) throws Exception {
        HtmlPage htmlPage = webClient.getPage(url);
        return htmlPage.getWebResponse().getContentAsString();
    }

    @Override
    public void close() throws Exception {
        webClient.close();
    }
}
