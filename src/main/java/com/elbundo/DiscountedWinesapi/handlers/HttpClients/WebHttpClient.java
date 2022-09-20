package com.elbundo.DiscountedWinesapi.handlers.HttpClients;

import com.gargoylesoftware.htmlunit.CookieManager;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.parser.HTMLParserListener;
import org.apache.commons.logging.LogFactory;

import java.net.URL;
import java.util.logging.Level;

public class WebHttpClient implements HttpClient{
    WebClient webClient;
    public WebHttpClient() {
        java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.OFF);
        java.util.logging.Logger.getLogger("org.apache.commons.httpclient").setLevel(Level.OFF);
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
