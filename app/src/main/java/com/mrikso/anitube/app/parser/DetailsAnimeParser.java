package com.mrikso.anitube.app.parser;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mrikso.anitube.app.model.AnimeDetailsModel;
import com.mrikso.anitube.app.model.AnimeMobileDetailsModel;
import com.mrikso.anitube.app.model.BaseAnimeModel;
import com.mrikso.anitube.app.model.DubbersTeam;
import com.mrikso.anitube.app.model.FranchiseModel;
import com.mrikso.anitube.app.model.ScreenshotModel;
import com.mrikso.anitube.app.model.SimpleDetailAnimeModel;
import com.mrikso.anitube.app.model.SimpleModel;
import com.mrikso.anitube.app.model.WatchAnimeStatusModel;
import com.mrikso.anitube.app.utils.ParserUtils;

import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.internal.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.rxjava3.core.Single;

public class DetailsAnimeParser {
    private static final String TAG = "DetailsAnimeParser";
    private final String LIST_PATTERN = "new(\\s)MyLists\\(\\'(.*)\\',(.*)\\)";
    private final String DESCRIPTION_PATTERN = "\"description\"\\s*:\\s*\\\"([.+]|[\\s\\S]*)\",([\\s\\S]*)\"duration\"\\s*";
    private final String TITLE_PATTERN = "\"name\"\\s*:\\s*\\\"([.+]|[\\s\\S]*)\",([\\s\\S]*)\"@id\"\\s*";

    public Single<AnimeDetailsModel> getDetailsModel(Document doc) {
        return Single.fromCallable(() -> parseAnimePage(doc));
    }

    public Single<AnimeMobileDetailsModel> getMobileDetailsModel(Document doc) {
        return Single.fromCallable(() -> parseMobileAnimePage(doc));
    }

    private AnimeDetailsModel parseAnimePage(Document doc) {
        Element rootContentElement = doc.selectFirst("div.content");
        Element storyElement = rootContentElement.selectFirst("#dle-content > article.story");

        String jsonDetails = rootContentElement.selectFirst("script[type=application/ld+json]").html();

        String name = ParserUtils.getMatcherResult(TITLE_PATTERN, jsonDetails, 1);
        if (name != null) {
            jsonDetails = jsonDetails.replace(name, StringEscapeUtils.escapeJson(name));
        }
        String description = ParserUtils.getMatcherResult(DESCRIPTION_PATTERN, jsonDetails, 1);
        if (description != null) {
            jsonDetails = jsonDetails.replace(description, StringEscapeUtils.escapeJson(description));
        }
        // Log.d(TAG, jsonDetails);

        GsonBuilder builder = new GsonBuilder();
        //builder.registerTypeAdapter(String.class, new EscapeStringSerializer());
        //builder.disableHtmlEscaping();
        Gson gson = builder.create();

        SimpleDetailAnimeModel simpleDetailAnimeModel = gson.fromJson(jsonDetails, SimpleDetailAnimeModel.class);

        // #dle-content > article > div:nth-child(1) > div.story_c
        Element storyPostElement = storyElement.selectFirst("div.story_c_left");
        String animeUrl = simpleDetailAnimeModel.getUrl();
        Element titleElement = storyElement.selectFirst("div.story_c h2");

        int animeId = ParserUtils.getAnimeId(simpleDetailAnimeModel.getId());

        AnimeDetailsModel animeDetailsModel = new AnimeDetailsModel(animeId, simpleDetailAnimeModel.getName(), simpleDetailAnimeModel.getThumbnail(), simpleDetailAnimeModel.getUrl());
        animeDetailsModel.setDescription(StringEscapeUtils.unescapeJson(simpleDetailAnimeModel.getDescription()));

        String trailerUrl = simpleDetailAnimeModel.getEmbedUrl();
        if (!Strings.isNullOrEmpty(trailerUrl)) {
            trailerUrl = StringEscapeUtils.unescapeHtml4(trailerUrl);
            String trailerPreviewUrl = buildPreviewUrl(trailerUrl);

            // трейлер може бути завантажено не на ютуб, тому беремо превь'ю з сайту
            if (Strings.isNullOrEmpty(trailerPreviewUrl)) {
                Element trailerPrewiewElement = storyElement.selectFirst("div.trailer_preview");
                trailerPreviewUrl = ParserUtils.getImageUrl(trailerPrewiewElement);
            }
            ScreenshotModel trailerModel = new ScreenshotModel(trailerPreviewUrl, trailerUrl);
            animeDetailsModel.setTrailerModel(trailerModel);
            // Log.d(TAG, trailerModel.toString());
        }

        Element playlistsAjaxElement = storyElement.selectFirst("div.playlists-ajax");
        if (playlistsAjaxElement != null) {
            animeDetailsModel.setHavePlaylistsAjax(true);
        }

        Element ageElement = storyPostElement.selectFirst("span.story_age");
        if (ageElement != null) {
            String age = storyPostElement.getElementsByTag("sup").first().text().trim();
            animeDetailsModel.setAge(age);
        }

        Element favElementHeader = titleElement.selectFirst("ul > li > a");
        if (favElementHeader != null) {
            boolean isFav = favElementHeader.attr("href").contains("doaction=del");
            animeDetailsModel.setFavorites(isFav);
        }

        Element storyElementDetail = storyElement.selectFirst("div.story_c");

        parseDetails(storyElementDetail, animeDetailsModel);

        Element screenshotElement = storyElement.selectFirst("#story_screen_list1");
        if (screenshotElement != null) {
            parseScreenshotsBlock(screenshotElement, animeDetailsModel);
        }

        Element taggersElement = storyElement.selectFirst("div.tagers");
        if (taggersElement != null) {

            Element tagsElement = taggersElement.selectFirst("span");
            if (tagsElement != null) {
                Element teamListByTag = tagsElement.getElementsByTag("a").first();
                String seasonName = teamListByTag.text();
                String seasonUrl = teamListByTag.attr("href");
                animeDetailsModel.setAnimeSeason(new SimpleModel(seasonName, seasonUrl));
            }
            String updateDate = taggersElement.selectFirst("div.story_ico_time").text().trim();
            animeDetailsModel.setLastUpdateTime(updateDate);
        }

        parseRelatedBlock(storyElement.selectFirst("div.box"), animeDetailsModel, animeUrl);

        animeDetailsModel.setRating(ParserUtils.parseRatingBlock(rootContentElement));

        // витягування схожих аніме
        // body > div.content > div:nth-child(10) > div > ul
        // #dle-content > article > div:nth-child(8) > div > ul
        Element similarsElement = doc.selectFirst("div > ul.portfolio_items");
        if (similarsElement != null) {
            parseSimilarBlock(similarsElement, animeDetailsModel);
        }

        // body > div.content > div:nth-child(9) > div > center > a
        Element torrentsElement = rootContentElement.selectFirst("div.box > div.my-text");
        if (torrentsElement != null) {
            animeDetailsModel.setHasTorrent(true);
            String torrentPageUrl = torrentsElement.selectFirst("a").attr("href");
            animeDetailsModel.setTorrentPageUrl(torrentPageUrl);
        }

        String listContent = ParserUtils.getMatcherResult(LIST_PATTERN, doc.html(), 2);
        if (!Strings.isNullOrEmpty(listContent)) {
            String[] listContentSplit = listContent.split("\\|");
            for (String element : listContentSplit) {

                if (!Strings.isNullOrEmpty(element)) {
                    String[] elementSplit = element.split("/"); // 0 index - id, 1 index - status

                    if (StringUtil.isNumeric(elementSplit[0])) {

                        if (Integer.parseInt(elementSplit[0]) == animeId) {
                            int currentStatus = Integer.parseInt(elementSplit[1]);
                            WatchAnimeStatusModel watchModel = ParserUtils.getWatchModel(currentStatus);
                            animeDetailsModel.setWatchStatusModdel(watchModel);
                        }
                    }
                }
            }
        }

        //animeDetailsModel.setTrailerModel(trailerModel);

        //Gson gson = new GsonBuilder().setPrettyPrinting().create();
        //String json = gson.toJson(animeDetailsModel);
        //Log.i(TAG, json);
        return animeDetailsModel;
    }

    private AnimeMobileDetailsModel parseMobileAnimePage(Document doc) {
        AnimeMobileDetailsModel model = new AnimeMobileDetailsModel();
        Element rootContentElement = doc.selectFirst("div#dle-content");
        Element fullPageElement = rootContentElement.selectFirst("div.full_page");
        String updateStatus = Objects.requireNonNull(fullPageElement.selectFirst("div.to_view")).text();

        Element typeElement = fullPageElement.selectFirst("li.vis:has(span:containsOwn(Тип))");
        if (typeElement != null) {
            SimpleModel year = ParserUtils.buidlSimpleModel(
                    typeElement.selectFirst("a"));
            model.setAnimeType(year);
        }
        //#full_info > ul > li:nth-child(2) > span
        model.setAnimeUpdateStatus(updateStatus);
        //#full_poster > img

        Element posterElement = fullPageElement.selectFirst("#full_poster > img");
        if (posterElement != null) {
            String posterUrl = posterElement.attr("src");
            model.setPosterUrl(posterUrl);
        }

        return model;
    }

    private void parseDetails(Element storyElements, AnimeDetailsModel model) {

        // витягування оригінальної назви
        Element originalTitleElement = storyElements.selectFirst("div.story_c_r strong:contains(Оригінальна назва)");
        if (originalTitleElement != null) {
            String originalTitle = originalTitleElement.nextSibling().toString().trim();
            model.setOriginalTitle(originalTitle);
        }

        // витягування року випуску аніме
        Element yearElement = storyElements.selectFirst("div.story_c_r strong:contains(Рік)");
        if (yearElement != null) {
            SimpleModel year = ParserUtils.buidlSimpleModel(
                    yearElement.nextElementSibling().selectFirst("a"));
            model.setReleaseYear(year);
        }

        // витягування жанру
        Elements genreLinksElement = storyElements
                .selectFirst("div.story_c_r strong:contains(Жанр:)")
                .nextElementSiblings()
                .select("a[href*=/anime/]");
        if (genreLinksElement != null) {
            List<SimpleModel> genre = ParserUtils.getSimpleDataFromElements(genreLinksElement);
            model.setGenres(genre);
        }

        // витягування режисера
        Element directorElement = storyElements.selectFirst("div.story_c_r strong:contains(Режисер)");
        if (directorElement != null) {
            String director = directorElement.nextSibling().toString().trim();
            model.setDirector(director);
        }

        // витягування студії
        Element studioElement =
                storyElements.select("div.story_c_r strong:contains(Студія)").first();
        if (studioElement != null) {
            String studio = studioElement.nextSibling().toString().trim();
            model.setStudio(studio);
        }

        // витягування кількості серій
        Element episodesElement =
                storyElements.select("div.story_c_r strong:contains(Серій)").first();
        if (episodesElement != null) {
            String episodes = episodesElement.nextSibling().toString().trim();
            model.setEpisodes(episodes);
        }

        // витягування перекладу
        Elements translationElement = storyElements.select("div.story_c_r a[href*=/translation/]");
        if (translationElement != null) {
            List<SimpleModel> translation = ParserUtils.getSimpleDataFromElements(translationElement);
            model.setTranslators(translation);
        }

        // витягування озвучення
        Element voicedElement = storyElements.selectFirst("div.story_c_r strong:contains(Ролі озвучували)");
        if (voicedElement != null) {
            parseDubbersBlock(voicedElement, model);
        }

        // витягування опису
        /*Element descriptionElement = storyElements.selectFirst("div.my-text");
        if (descriptionElement != null) {
            String description = descriptionElement.text().trim();
            model.setDescription(description);
        }*/
    }

    private void parseDubbersBlock(Element voicedElement, AnimeDetailsModel model) {
        Elements voicedElements = voicedElement.nextElementSiblings();
        // Log.i(TAG, "print voicedElements");
        //  Log.i(TAG, voicedElements.html());
        Elements teamLists = voicedElements.select("span.team_list");
        if (!teamLists.isEmpty()) {
            List<DubbersTeam> dubbersTeamList = new ArrayList<>(teamLists.size());

            // Log.i(TAG, "print teamLists");
            // Log.i(TAG, teamLists.html());
            for (Element teamList : teamLists) {

                Element teamListByTag = teamList.getElementsByTag("a").first();
                String teamName = teamListByTag.text();
                String teamUrl = teamListByTag.attr("href");
                // Log.i(TAG, teamName + " " + teamUrl);
                SimpleModel dubberTeam = new SimpleModel(teamName, teamUrl);
                Elements teams = teamList.select("a.teams");
                List<SimpleModel> dubbers = ParserUtils.getDataFromAttr(teams);

                DubbersTeam dubbersTeam = new DubbersTeam(dubberTeam, dubbers);
                dubbersTeamList.add(dubbersTeam);
            }
            model.setDubbersTeamList(dubbersTeamList);
        } else {
            List<SimpleModel> voiced = ParserUtils.getDataFromAttr(voicedElements.select("a[href*=/voiced/]"));
            if (!voiced.isEmpty()) {
                model.setVoicers(voiced);
            }
        }
    }

    private void parseScreenshotsBlock(Element screenshotModel, AnimeDetailsModel model) {
        Elements elements = screenshotModel.getElementsByTag("a");
        List<ScreenshotModel> screenshotsList = new ArrayList<>(elements.size());
        for (Element element : elements) {
            String screenshotFullUrl = element.attr("href");
            String screenshotMiniUrl = ParserUtils.getImageUrl(element);
            ScreenshotModel screenshot = new ScreenshotModel(screenshotMiniUrl, screenshotFullUrl);
            screenshotsList.add(screenshot);
        }

        model.setScreenshotsModel(screenshotsList);
    }

    private void parseSimilarBlock(Element element, AnimeDetailsModel model) {
        Elements elements = element.getElementsByTag("li");
        List<BaseAnimeModel> similarsList = new ArrayList<>(elements.size());
        for (Element similar : elements) {
            String posterUrl = ParserUtils.getImageUrl(similar.selectFirst("div.sl_poster"));
            Element textElement = similar.selectFirst("div.text_content a");
            String title = textElement.text();
            String animeUrl = textElement.attr("href");
            int animeId = ParserUtils.getAnimeId(animeUrl);
            BaseAnimeModel similarModel = new BaseAnimeModel(animeId, title, posterUrl, animeUrl);
            similarsList.add(similarModel);
        }

        model.setSimilarAnimeList(similarsList);
    }

    private void parseRelatedBlock(Element element, AnimeDetailsModel model, String currentUrl) {
        Element relatedElement = element.children().last(); //
        if (relatedElement.html().contains("news fran")) {
            FranchiseParser fran = new FranchiseParser();
            List<FranchiseModel> franchises = fran.parseFranchises(currentUrl, relatedElement);
            model.setFranchiseList(franchises);
        }
    }

    @Nullable
    private String buildPreviewUrl(@NonNull String previewUrl) {
        // Використовуємо регулярний вираз, щоб отримати ID відео
        Pattern pattern = Pattern.compile(".*\\/([a-zA-Z0-9_-]{11}).*");
        Matcher matcher = pattern.matcher(previewUrl);

        if (matcher.matches()) {
            String videoId = matcher.group(1);
            return String.format("https://img.youtube.com/vi/%s/hqdefault.jpg", videoId);
        } else {
            return null;
        }
    }
}
