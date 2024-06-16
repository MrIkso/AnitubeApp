package com.mrikso.anitube.app.parser;

import android.util.Log;

import com.google.gson.Gson;
import com.mrikso.anitube.app.model.QickSearchResponse;
import com.mrikso.anitube.app.model.SimpleModel;

import org.jsoup.Jsoup;
import org.jsoup.internal.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class QuickSearchResultParser {

    public  QuickSearchResultParser() {}

    public List<SimpleModel> getQuickSearchResults(String json) {
        List<SimpleModel> quickSearchResultList = new ArrayList<>();
        Gson gson = new Gson();
        QickSearchResponse response = gson.fromJson(json, QickSearchResponse.class);
        String resultHtml = response.getContent();
        if (resultHtml != null && !StringUtil.isBlank(resultHtml)) {
            Document doc = Jsoup.parse(resultHtml);
            Elements resultsElements = doc.select("a");
            for (Element element : resultsElements) {
                String url = element.attr("href");
                // Log.i("QuickSearchResultParser", element.html());
                Element searchHeadEl = element.selectFirst("span.searchheading");
                if (searchHeadEl != null) {
                    String name = searchHeadEl.text().trim();
                    quickSearchResultList.add(new SimpleModel(name, url));
                }
            }
            return quickSearchResultList;
        } else {
            //Log.i("QuickSearchResultParser", response.getText());
        }
        return quickSearchResultList;
    }
}
