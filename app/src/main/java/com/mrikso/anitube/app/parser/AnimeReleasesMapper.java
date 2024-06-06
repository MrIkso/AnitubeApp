package com.mrikso.anitube.app.parser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mrikso.anitube.app.model.AnimeListReleases;
import com.mrikso.anitube.app.model.AnimeReleaseModel;
import com.mrikso.anitube.app.model.UserModel;
import com.mrikso.anitube.app.utils.ParserUtils;
import com.mrikso.anitube.app.utils.PreferencesHelper;

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
        AnimeListReleases releases = storyParser.getReleases(document);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Type responseType = new TypeToken<List<AnimeReleaseModel>>() {}.getType();

        String json = gson.toJson(releases);
        // Log.i("AnimeReleasesMapper", json);
        return releases;
    }

    public UserModel getUserData() {
        return homePage.getUser();
    }
}
