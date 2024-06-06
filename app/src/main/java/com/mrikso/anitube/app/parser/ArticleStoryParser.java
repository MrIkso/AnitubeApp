package com.mrikso.anitube.app.parser;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mrikso.anitube.app.model.AnimeListReleases;
import com.mrikso.anitube.app.model.AnimeReleaseModel;
import com.mrikso.anitube.app.model.SimpleModel;
import com.mrikso.anitube.app.utils.ParserUtils;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class ArticleStoryParser {
    private final String TAG = "ArticleStoryParser";

    public AnimeListReleases getReleases(Document document) {
        List<AnimeReleaseModel> list = getAnimeModels(document.select("article.story"));
        AnimeListReleases releases = new AnimeListReleases(list);
        Element navigationElement = document.selectFirst("div.navigation > span.navi_pages");
        if (navigationElement != null) {
            String maxPage = navigationElement.getElementsByTag("a").last().text();
            releases.setMaxPage(Integer.parseInt(maxPage));
        } else {
            releases.setMaxPage(1);
        }
        return releases;
    }

    public List<AnimeReleaseModel> getAnimeModels(Elements storyElements) {
        List<AnimeReleaseModel> models = new ArrayList<>();
        for (Element storyElement : storyElements) {
            models.add(getAnimeModel(storyElement));
        }
        return models;
    }

    public AnimeReleaseModel getAnimeModel(Element storyElement) {
        // Log.i(TAG, storyElement.html());
        AnimeReleaseModel releaseModel = null;
        Element storyPostElement = storyElement.selectFirst("div.story_c");
        Element titlElement = storyElement.selectFirst("h2");
        if (titlElement != null) {
            String title = titlElement.text();
            String url = titlElement.getElementsByTag("a").first().attr("href");
            releaseModel = new AnimeReleaseModel(ParserUtils.getAnimeId(url), title, url);

            Element favStatus = titlElement.selectFirst("ul > li > a");
            if (favStatus != null) {
                boolean isFav = favStatus.attr("href").contains("doaction=del");
                releaseModel.setFavorites(isFav);
            }
        }

        Element posterElement = storyPostElement.selectFirst(".story_c_l");
        if (posterElement != null) {
            String urlPoster = ParserUtils.getImageUrl(posterElement);
            releaseModel.setPosterUrl(urlPoster);
            // #dle-content > article:nth-child(1) > div > div > div.story_c_l >
            // span.story_astatus_yellow
            Element status = posterElement.selectFirst("span[class*=\"story_astatus\"] > sup");
            if (status != null) {
                releaseModel.setWatchStatusModdel(ParserUtils.getWatchModel(status.text()));
            }
        }

        // витягування року випуску аніме
        Element yearElement = storyElement.selectFirst("div.story_infa dt:contains(Рік)");
        if (yearElement != null) {
            SimpleModel year = ParserUtils.buidlSimpleModel(
                    yearElement.nextElementSibling().selectFirst("a"));
            releaseModel.setReleaseYear(year);
        }

        // витягування кількості епізодів та їх тривалості
        String episodes = storyElement
                .selectFirst("div.story_infa dt:contains(Серій)")
                .nextSibling()
                .toString()
                .trim();

        releaseModel.setEpisodes(episodes);
        // витягування рейтингу
        String rating = ParserUtils.parseRatingBlock(storyElement);
        releaseModel.setRating(rating);

        // витягування опису
        Element descriptionElement = storyElement.selectFirst("div.story_c_text");
        if (descriptionElement != null) {
            String description = descriptionElement.text().trim();
            releaseModel.setDescription(description);
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(releaseModel);
        Log.i(TAG, json);
        return releaseModel;
    }
}
