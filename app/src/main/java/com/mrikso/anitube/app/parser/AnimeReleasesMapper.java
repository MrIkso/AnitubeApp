package com.mrikso.anitube.app.parser;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mrikso.anitube.app.model.AnimeListReleases;
import com.mrikso.anitube.app.model.AnimeReleaseModel;

import org.jsoup.nodes.Document;

import java.lang.reflect.Type;
import java.util.List;

public class AnimeReleasesMapper {

    public AnimeListReleases transform(Document document) {

        ArticleStoryParser storyParser = new ArticleStoryParser();
        AnimeListReleases releases = storyParser.getReleaeses(document);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Type responseType = new TypeToken<List<AnimeReleaseModel>>() {}.getType();

        String json = gson.toJson(releases);
        Log.i("AnimeReleasesMapper", json);
        return releases;
    }
}
