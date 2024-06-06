package com.mrikso.anitube.app.parser;

import androidx.core.util.Pair;

import com.mrikso.anitube.app.model.CollectionModel;
import com.mrikso.anitube.app.utils.ParserUtils;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class CollectionsParser {

    public CollectionsParser() {}

    public Pair<Integer, List<CollectionModel>> transform(Document data) {
        int maxPage = 1;
        Elements collectionsElements = data.select("#dle-content > div.box");
        List<CollectionModel> collections = new ArrayList<>(collectionsElements.size());
        for (Element collectionElement : collectionsElements) {
            CollectionModel collectionModel = new CollectionModel();
            Element story_c = collectionElement.selectFirst("div.story_c");
            Element titleElement = story_c.selectFirst("h2 > a");
            String name = titleElement.text();
            String url = titleElement.attr("href");
            // #dle-content > div:nth-child(19) > div.story_c > div > div.story_c_l >
            // span.story_post
            Element postElement = story_c.selectFirst("span.story_post");
            String posterUrl = ParserUtils.getImageUrl(postElement);
            // #dle-content > div:nth-child(1) > div.col_news > span
            int count = Integer.parseInt(
                    collectionElement.selectFirst("div.col_news > span").text());
            // #dle-content > div:nth-child(1) > div.story_ico_prof > span
            Element authorElement = collectionElement.selectFirst("div.story_ico_prof");

            if (authorElement != null) {
                collectionModel.setAuthor(authorElement.text().trim());
            }

            collectionModel.setNameCollection(name);
            collectionModel.setCollectionUrl(url);
            collectionModel.setPosterUrl(posterUrl);
            collectionModel.setCountAnime(count);
            collections.add(collectionModel);
        }
        Element navigationElement = data.selectFirst("div.navigation > span.navi_pages");
        if (navigationElement != null) {
            String maxpage = navigationElement.getElementsByTag("a").last().text();
            maxPage = Integer.parseInt(maxpage);
        }
        return new Pair<>(maxPage, collections);
    }
}
