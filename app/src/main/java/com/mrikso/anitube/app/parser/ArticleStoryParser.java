package com.mrikso.anitube.app.parser;

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

    public AnimeListReleases getReleaeses(Document document) {
        List<AnimeReleaseModel> list = getAnimeModels(document.select("article.story"));
        AnimeListReleases releases = new AnimeListReleases(list);
        Element navigationElement = document.selectFirst("div.navigation > span.navi_pages");
        if (navigationElement != null) {
            //  String currentPage = navigationElement.selectFirst("span > a.title").text().trim();
            String maxPage = navigationElement.getElementsByTag("a").last().text();
            // releases.setCurrentPage(currentPage);
            releases.setMaxPage(maxPage);
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
        AnimeReleaseModel releaseModel = new AnimeReleaseModel();
        Element storyPostElement = storyElement.selectFirst("div.story_c");
        Element titlElement = storyElement.selectFirst("h2");
        if (titlElement != null) {
            String title = titlElement.text();
            String url = titlElement.getElementsByTag("a").first().attr("href");
            String[] parts = url.split("-");
            int id = Integer.parseInt(parts[0].substring(parts[0].lastIndexOf("/") + 1));
            releaseModel.setAnimeId(id);
            releaseModel.setAnimeUrl(url);
            releaseModel.setTitle(title);
        }

        Element posterElement = storyPostElement.selectFirst(".story_c_l");
        if (posterElement != null) {
            String urlPoster = ParserUtils.getImageUrl(posterElement);
            releaseModel.setPosterUrl(urlPoster);
        }

        // витягування року випуску аніме
        Element yearElement = storyElement.selectFirst("div.story_infa dt:contains(Рік)");
        if (yearElement != null) {
            SimpleModel year =
                    ParserUtils.buidlSimpleModel(yearElement.nextElementSibling().selectFirst("a"));
            releaseModel.setReleaseYear(year);
        }

        // витягування кількості епізодів та їх тривалості
        String episodes =
                storyElement
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
        // Log.i(TAG, releaseModel.toString());
        return releaseModel;
    }
}
