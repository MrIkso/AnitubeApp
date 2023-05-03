package com.mrikso.anitube.app.parser;

import android.util.Log;

import androidx.core.util.Pair;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mrikso.anitube.app.model.AnimeListReleases;
import com.mrikso.anitube.app.model.AnimeReleaseModel;
import com.mrikso.anitube.app.utils.ParserUtils;
import com.mrikso.anitube.app.utils.PreferencesHelper;

import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;

import org.jsoup.nodes.Document;

import java.lang.reflect.Type;
import java.util.List;

public class AnimeReleasesMapper {

    private Subject<Pair<String, String>> userData = PublishSubject.create();

    public AnimeReleasesMapper() {}

    public AnimeListReleases transform(Document document) {

        ParserUtils.parseDleHash(document.html());
        if (PreferencesHelper.getInstance().isLogin()) {

            Pair<String, String> data = new HomePageParser(document).getUserData();
            if (data != null) {
                this.userData.onNext(data);
            }
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
        return userData;
    }
}
