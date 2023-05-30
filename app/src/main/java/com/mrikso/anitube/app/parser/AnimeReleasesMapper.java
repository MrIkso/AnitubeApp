package com.mrikso.anitube.app.parser;

import androidx.core.util.Pair;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mrikso.anitube.app.model.AnimeListReleases;
import com.mrikso.anitube.app.model.AnimeReleaseModel;
import com.mrikso.anitube.app.utils.ParserUtils;
import com.mrikso.anitube.app.utils.PreferencesHelper;

import io.reactivex.rxjava3.subjects.Subject;

import org.jsoup.nodes.Document;

import java.lang.reflect.Type;
import java.util.List;

public class AnimeReleasesMapper {

    private HomePageParser homePage;

    public AnimeReleasesMapper() {
        homePage = HomePageParser.getInstance();
    }

    public AnimeListReleases transform(Document document) {

        ParserUtils.parseDleHash(document.html());
        if (PreferencesHelper.getInstance().isLogin()) {
            homePage.parseUserData(document);
            //  Log.i("mapper", userData.toString());
        }
        ArticleStoryParser storyParser = new ArticleStoryParser();
        AnimeListReleases releases = storyParser.getReleaeses(document);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Type responseType = new TypeToken<List<AnimeReleaseModel>>() {}.getType();

        String json = gson.toJson(releases);
        // Log.i("AnimeReleasesMapper", json);
        return releases;
    }

    public Subject<Pair<String, String>> getUserData() {
        return homePage.getUser();
    }
}
