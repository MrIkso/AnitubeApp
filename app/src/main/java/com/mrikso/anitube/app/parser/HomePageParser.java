package com.mrikso.anitube.app.parser;

import android.util.Log;

import com.mrikso.anitube.app.model.AnimeReleaseModel;
import com.mrikso.anitube.app.model.BaseAnimeModel;
import com.mrikso.anitube.app.model.CollectionModel;
import com.mrikso.anitube.app.model.InteresingModel;
import com.mrikso.anitube.app.model.LoadState;
import com.mrikso.anitube.app.model.SimpleModel;
import com.mrikso.anitube.app.model.UserModel;
import com.mrikso.anitube.app.utils.ParserUtils;
import com.mrikso.anitube.app.utils.PreferencesHelper;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class HomePageParser {
    private final String TAG = "HomePageParser";
    private final List<InteresingModel> interestingAnimeList = new ArrayList<>();
    private final List<BaseAnimeModel> bestAnimeList = new ArrayList<>();
    private final List<AnimeReleaseModel> releasesAnimeList = new ArrayList<>();
    private final List<CollectionModel> newCollectionsList = new ArrayList<>();
    private List<SimpleModel> genresList = new ArrayList<>();
    private List<SimpleModel> calendarList = new ArrayList<>();
    private  UserModel userData = null;
           // = PublishSubject.create();

    private LoadState state;

    private static HomePageParser homePageParser;

    public static HomePageParser getInstance() {
        if (homePageParser == null) {
            homePageParser = new HomePageParser();
        }
        return homePageParser;
    }

    public HomePageParser() {}

    public void parseHome(Document doc) {
        interestingAnimeList.clear();
        bestAnimeList.clear();
        releasesAnimeList.clear();
        newCollectionsList.clear();
        genresList.clear();
        calendarList.clear();

        //		if(doc.select("div.top") != null){
        //			state = LoadState.ERROR;
        //			return;
        //		}
        ParserUtils.parseDleHash(doc.html());
        parseUserData(doc);

        Elements slideBody =
                doc.getElementById("header_img").select("div.slide_block").select("div.slide_body");
        Elements sliders = slideBody.first().getElementsByTag("a");

        for (Element slider : sliders) {
            String url = slider.attr("href");
            String imageUrl = slider.getElementsByTag("img").first().attr("src");
            // Log.d(TAG, url + "=>" + imageUrl);
            InteresingModel interesting = new InteresingModel(imageUrl, url);
            interestingAnimeList.add(interesting);
        }
        Elements navControlsElement = doc.select("#header_menu > div > div.inc_tab > div.case.visible.genres");
        if (navControlsElement != null) {
            parseGenresAndCalendarList(navControlsElement);
        }
        Element contentElement = doc.selectFirst("div.content");
        Elements bestAnimes = contentElement
                .select("div.box")
                .first()
                .select("div.example")
                .select("div.carousel")
                .select("div.carousel_container")
                .select("ul.portfolio_items")
                .select("li");

        for (Element beastAnime : bestAnimes) {
            Element posterAnimeElement = beastAnime.selectFirst("div.sl_poster");
            String animeUrl = posterAnimeElement.getElementsByTag("a").attr("href");
            String urlPoster = posterAnimeElement.getElementsByTag("img").attr("src");
            String animeTitle = beastAnime
                    .selectFirst("div.text_content")
                    .getElementsByTag("a")
                    .text();
            //Log.d(TAG, urlPoster + " " + animeUrl + " " + animeTitle);
            bestAnimeList.add(new BaseAnimeModel(ParserUtils.getAnimeId(animeUrl), animeTitle, urlPoster, animeUrl));
        }

        Elements newsAnimes = contentElement.select("div.news_2");
        //	Log.d(TAG, newsAnimes.html());
        for (Element newAmime : newsAnimes) {

            Element title = newAmime.select("div.title2").first();
            if (title != null) {
                Element title_a = title.getElementsByTag("a").first();

                String animeUrl = title_a.attr("href");
                String animeTitle = title_a.text();
                int animeId = ParserUtils.getAnimeId(animeUrl);
                // витягування статусу аніме в списку
                // body > div.content > div.box.lcol > div:nth-child(2) > div.news_2_c >
                // div.news_2_c_l > a > span
                Element news_2_c_l = newAmime.selectFirst("div.news_2_c_l");
                String urlPoster = news_2_c_l.getElementsByTag("img").first().attr("src");

                AnimeReleaseModel animeRelease = new AnimeReleaseModel(animeId, animeTitle, urlPoster, animeUrl);
                Element statusAnimeElement = news_2_c_l.selectFirst("a > span");
                if (statusAnimeElement != null) {
                    String statusAnime = statusAnimeElement.text();
                    animeRelease.setWatchStatusModdel(ParserUtils.getWatchModel(statusAnime));
                }

                Element infaBase = newAmime.selectFirst("div.news_2_c_r");
                Element infa = infaBase.selectFirst("div.news_2_c_inf");

                // витягування року випуску аніме
                Element yearElement = infa.selectFirst("div.news_2_infa dt:contains(Рік) + a");
                if (yearElement != null) {
                    SimpleModel releaseYear = new SimpleModel(yearElement.text().trim(), null);
                    animeRelease.setReleaseYear(releaseYear);
                } else {
                    // body > div.content > div.box.lcol > div:nth-child(2) > div.news_2_c >
                    // div.news_2_c_r > div.news_2_c_inf > div.news_2_infa > a:nth-child(2)
                    Element yearElementReg = infa.selectFirst("div.news_2_infa > a:nth-child(2)");
                    if (yearElementReg != null) {
                        SimpleModel releaseYear =
                                new SimpleModel(yearElementReg.text().trim(), null);
                        animeRelease.setReleaseYear(releaseYear);
                    }
                }

                // Отримання кількості серій та їх тривалості
                String episodes = infa.select("div.news_2_infa dt:contains(Серій:)")
                        .first()
                        .nextSibling()
                        .toString()
                        .trim();

                Element newsElement = infaBase.selectFirst(".news_2_c_text");
                String description = newsElement.text().replace("Опис:", "").trim();

                animeRelease.setEpisodes(episodes);
                animeRelease.setDescription(description);

                releasesAnimeList.add(animeRelease);
                //Log.d(TAG, animeRelease.toString());
            }
        }

        Element collectionlElement = contentElement.selectFirst("div.colekcii > ul > center");
        if (collectionlElement != null) {
            parseNewCollections(collectionlElement);
        }

        state = LoadState.DONE;
    }

    public void parseUserData(Document doc) {
        if (PreferencesHelper.getInstance().isLogin()) {
            ParserUtils.parseDleHash(doc.html());
            UserModel data = getUserData(doc);
            if (data != null) userData = (data);
        }
    }

    private void parseGenresAndCalendarList(Elements elements) {

        Element genres = elements.get(0);
        genresList = ParserUtils.getDataFromAttr(genres.selectFirst("ul"));

        Element calendar = elements.get(1);
        calendarList = ParserUtils.getDataFromAttr(calendar.selectFirst("ul"));
    }

    private void parseNewCollections(Element collectiosElement) {
        Elements collectionsEls = collectiosElement.getElementsByTag("a");
        for (Element element : collectionsEls) {
            String url = element.attr("href");
            Element poster = element.selectFirst("div.li_poster");
            String posterUrl = ParserUtils.getImageUrl(poster);
            int count = Integer.parseInt(poster.selectFirst("span.comm > sup").text());
            String name = element.selectFirst("div.li_text").text();

            CollectionModel collectionModel = new CollectionModel();
            collectionModel.setCollectionUrl(url);
            collectionModel.setNameCollection(name);
            collectionModel.setPosterUrl(posterUrl);
            collectionModel.setCountAnime(count);

            newCollectionsList.add(collectionModel);
            //Log.d(TAG, collectionModel.toString());
        }
    }

    public UserModel getUserData(Document doc) {
        // Log.i(TAG, "getUserData call");
        Element logFormElement = doc.selectFirst("#logform");
        if (logFormElement != null) {
            // Log.i(TAG, logFormElement.html());
            Element avaElement = logFormElement.selectFirst("div.log_ava > img");
            Element profileElement = logFormElement.selectFirst("div.log_links > ul > li > a");
            if (avaElement != null && profileElement != null) {
                String userName = avaElement.attr("alt");
                String avaUrl = avaElement.attr("src");
                String profileUrl = profileElement.attr("href");
                return new UserModel(userName, profileUrl, avaUrl);
            }
        }
        return null;
    }

    public List<InteresingModel> getInterestingAnime() {
        return this.interestingAnimeList;
    }

    public List<BaseAnimeModel> getBestAnime() {
        return this.bestAnimeList;
    }

    public List<AnimeReleaseModel> getLatestReleasesAnime() {
        return this.releasesAnimeList;
    }

    public List<CollectionModel> getNewCollectionsList() {
        return this.newCollectionsList;
    }

    public LoadState getLoadState() {
        return this.state;
    }

    public UserModel getUser() {
        return this.userData;
    }

    public List<SimpleModel> getGenresList() {
        return this.genresList;
    }

    public List<SimpleModel> getCalendarList() {
        return this.calendarList;
    }
}
