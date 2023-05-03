package com.mrikso.anitube.app.parser;

import android.util.Log;

import androidx.core.util.Pair;

import com.mrikso.anitube.app.model.AnimeReleaseModel;
import com.mrikso.anitube.app.model.BaseAnimeModel;
import com.mrikso.anitube.app.model.InteresingModel;
import com.mrikso.anitube.app.model.LoadState;
import com.mrikso.anitube.app.model.SimpleModel;
import com.mrikso.anitube.app.utils.ParserUtils;
import com.mrikso.anitube.app.utils.PreferencesHelper;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class HomePageParser {
    private final String TAG = "HomePageParser";
    private List<InteresingModel> interestingAnimeList = new ArrayList<>();
    private List<BaseAnimeModel> bestAnimeList = new ArrayList<>();
    private List<AnimeReleaseModel> releasesAnimeList = new ArrayList<>();
    private Pair<String, String> userData = new Pair<>(null, null);
    private LoadState state;

    private final Document doc;

    public HomePageParser(Document doc) {
        this.doc = doc;
    }

    public void parseHome() {
        interestingAnimeList.clear();
        bestAnimeList.clear();
        releasesAnimeList.clear();

        ParserUtils.parseDleHash(doc.html());

        if (PreferencesHelper.getInstance().isLogin()) {
            userData = getUserData();
        }

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

        Elements contentElement = doc.select("div.content");
        Elements bestAnimes =
                contentElement
                        .select("div.box")
                        .first()
                        .select("div.example")
                        .select("div.carousel")
                        .select("div.carousel_container")
                        .select("ul.portfolio_items")
                        .select("li");

        for (Element beastAnime : bestAnimes) {
            Element posterAnimeElement = beastAnime.selectFirst("div.sl_poster");
            String urlPoster = posterAnimeElement.getElementsByTag("a").attr("href");
            String imgUrl = posterAnimeElement.getElementsByTag("img").attr("src");
            String name = beastAnime.selectFirst("div.text_content").getElementsByTag("a").text();
            // Log.d(TAG, urlPoster + " " + imgUrl + " " + name);
            bestAnimeList.add(new BaseAnimeModel(name, imgUrl, urlPoster));
        }

        Elements newsAnimes = contentElement.select("div.news_2");
        //	Log.d(TAG, newsAnimes.html());
        for (Element newAmime : newsAnimes) {
            AnimeReleaseModel animeRelease = new AnimeReleaseModel();
            Element title = newAmime.select("div.title2").first();
            if (title != null) {
                Element title_a = title.getElementsByTag("a").first();

                String animeUrl = title_a.attr("href");
                String animeTitle = title_a.text();
                // витягування статусу аніме в списку
                // body > div.content > div.box.lcol > div:nth-child(2) > div.news_2_c >
                // div.news_2_c_l > a > span
                Element news_2_c_l = newAmime.selectFirst("div.news_2_c_l");

                Element statusAnimeElement = news_2_c_l.selectFirst("a > span");
                if (statusAnimeElement != null) {
                    String statusAnime = statusAnimeElement.text();
                    animeRelease.setWatchStatusModdel(ParserUtils.getWatchModel(statusAnime));
                }
                String urlPoster = news_2_c_l.getElementsByTag("img").first().attr("src");

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
                String episodes =
                        infa.select("div.news_2_infa dt:contains(Серій:)")
                                .first()
                                .nextSibling()
                                .toString()
                                .trim();

                Element newsElement = infaBase.selectFirst(".news_2_c_text");
                String description = newsElement.text().replace("Опис:", "").trim();

                animeRelease.setTitle(animeTitle);
                animeRelease.setPosterUrl(urlPoster);
                animeRelease.setAnimeUrl(animeUrl);
                animeRelease.setEpisodes(episodes);

                animeRelease.setDescription(description);

                releasesAnimeList.add(animeRelease);
                Log.d(TAG, animeRelease.toString());
            }
        }

        state = LoadState.DONE;
    }

    public Pair<String, String> getUserData() {
        Log.i(TAG, "getUserData call");
        Element logavaElement = doc.selectFirst("#logform");
        if (logavaElement != null) {
            Element ava = logavaElement.selectFirst("div.log_ava").getElementsByTag("img").first();
            String avaUrl = ava.attr("src");
            Element profileElement = logavaElement.selectFirst("div.log_links > ul > li > a");
            String profileUrl = profileElement.attr("href");
            return new Pair<>(avaUrl, profileUrl);
        }
        return null;
    }

    public List<InteresingModel> getInteresingAnime() {
        return this.interestingAnimeList;
    }

    public List<BaseAnimeModel> getBestAnime() {
        return this.bestAnimeList;
    }

    public List<AnimeReleaseModel> getLatestReleasesAnime() {
        return this.releasesAnimeList;
    }

    public LoadState getLoadState() {
        return this.state;
    }

    public Pair<String, String> getUser() {
        return this.userData;
    }
}
