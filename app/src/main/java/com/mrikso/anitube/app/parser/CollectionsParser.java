package com.mrikso.anitube.app.parser;

import androidx.core.util.Pair;

import com.mrikso.anitube.app.model.CollectionModel;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class CollectionsParser {

    public CollectionsParser() {}

    public Pair<Integer, List<CollectionModel>> transform(Document data) {
        int maxPage = 1;
        Elements collectionsElements = data.select("#dle-content > div:box");
        List<CollectionModel> collections = new ArrayList<>(collectionsElements.size());
        for (Element collectionElement : collectionsElements) {
            Element titleElement = collectionElement.selectFirst("div.story_c > h2 > a");
            String name = titleElement.text();
            String url = titleElement.attr("href");
            String posterUrl =
                    collectionElement
                            .selectFirst("div.story_c > div.story_c_l > span.story_post > img")
                            .attr("src");
            // #dle-content > div:nth-child(1) > div.col_news > span
            int count =
                    Integer.parseInt(collectionElement.selectFirst("div.col_news > span").text());

            CollectionModel collectionModel = new CollectionModel();
            collections.add(collectionModel);
        }
        Element navigationElement = data.selectFirst("div.navigation > span.navi_pages");
        if (navigationElement != null) {
            //  String currentPage = navigationElement.selectFirst("span > a.title").text().trim();
            String maxpage = navigationElement.getElementsByTag("a").last().text();
            // releases.setCurrentPage(currentPage);
            maxPage = Integer.valueOf(maxpage);
        }

        return new Pair<>(maxPage, collections);
    }
}
