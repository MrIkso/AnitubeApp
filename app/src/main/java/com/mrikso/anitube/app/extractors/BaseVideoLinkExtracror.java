package com.mrikso.anitube.app.extractors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public abstract class BaseVideoLinkExtracror {

    private final String url;
    private Document document;

    BaseVideoLinkExtracror(String url) throws IOException {
        this.url = url;
        document = Jsoup.connect(url).get();
    }

    public String extract() {
        return null;
    }

    protected Document getDocument() {
        return this.document;
    }
}
