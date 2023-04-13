package com.mrikso.anitube.app.utils;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mrikso.anitube.app.model.EpisodeModel;
import com.mrikso.anitube.app.model.PlayerJsonModelVideos;
import com.mrikso.anitube.app.model.VoicerModel;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParseVideosFromPage {
    private final String TAG = "ParseVideosFromPage";
    private final String PLAYER_PATTERN = "RalodePlayer\\.init\\((.*),(\\[\\[.*\\]\\])";
    private final String SRC_PATTERN = "src=\"([^\"]+)\"";
    private final String DUBBER_PATTERN = "\\(([^)]+)\\)";

    public List<VoicerModel> filesFromVideoContructor(Document document) {
        List<VoicerModel> playerlist = new ArrayList<>();
        // #dle-content > article > center > fieldset > div > script:nth-child(3)
        Elements elements = document.select("#dle-content > article script");

        String script =
                elements.stream()
                        .filter(e -> e.data().contains("RalodePlayer.init("))
                        .findFirst()
                        .orElse(null)
                        .data();

        // Log.i(TAG, script);

        Pattern pattern = Pattern.compile(PLAYER_PATTERN);
        Matcher matcher = pattern.matcher(script);
        String audiosJson = null;
        String videosJson = null;

        int count = 0;
        while (matcher.find()) {
            count++;
            audiosJson = matcher.group(1);
            // Log.i("ParseVideosFromPage", audiosJson);

            videosJson = matcher.group(2);
            //  Log.i("ParseVideosFromPage", videosJson);

        }
        if (count > 0) {
            Gson gson = new Gson();
            Type audiosListType = new TypeToken<List<String>>() {}.getType();
            Type videosListType = new TypeToken<List<List<PlayerJsonModelVideos>>>() {}.getType();
            List<String> audiosModel = gson.fromJson(audiosJson, audiosListType);
            List<List<PlayerJsonModelVideos>> videosModel =
                    gson.fromJson(videosJson, videosListType);

            for (int i = 0; i < audiosModel.size(); ++i) {
                String dubberNameRaw = audiosModel.get(i);
                Log.i(TAG, dubberNameRaw);
                String dubberName = null;
                if (dubberNameRaw.matches("(.*)(СУБ|ОЗВ)(.*)")) {
                    dubberName = dubberNameRaw;
                } else {
                    dubberName = ParserUtils.getMatcherResult(DUBBER_PATTERN, dubberNameRaw, 1);
                }
                List<PlayerJsonModelVideos> videos = videosModel.get(i);
                List<EpisodeModel> listAllEpisodes = new ArrayList<>();

                //  Log.i(TAG, dubberName);
                for (int j = 0; j < videos.size(); ++j) {
                    PlayerJsonModelVideos element = videos.get(j);

                    String url = ParserUtils.getMatcherResult(SRC_PATTERN, element.getCode(), 1);
                    //  Log.i(TAG, element.getName() + " " + url);
                    listAllEpisodes.add(
                            new EpisodeModel(String.valueOf(j), element.getName(), url));
                }

                VoicerModel voicerModel =
                        new VoicerModel(
                                String.valueOf(i), dubberName != null ? dubberName : dubberNameRaw);
                voicerModel.setEpisodes(listAllEpisodes);

                playerlist.add(voicerModel);
            }
        }
        return playerlist;
    }
}
