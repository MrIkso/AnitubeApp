package com.mrikso.anitube.app.extractors;

import com.mrikso.anitube.app.parser.ParserInterface;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import okhttp3.OkHttpClient;

import java.io.IOException;

public abstract class BaseVideoLinkExtracror implements ParserInterface {

    protected final String url;
    protected Document document;
    protected OkHttpClient client;

    BaseVideoLinkExtracror(String url) {
        this.url = url;
    }

    BaseVideoLinkExtracror(String url, OkHttpClient client) {
        this.url = url;
        this.client = client;
    }

    protected void extract() throws IOException {
        document = Jsoup.connect(url).get();
    }

    protected Document getDocument() {
        return this.document;
    }

    protected OkHttpClient getOkHttpClient() {
        return this.client;
    }

    protected String getUrl() {
        return url;
    }
}
