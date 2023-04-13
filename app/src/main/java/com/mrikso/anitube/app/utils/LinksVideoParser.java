package com.mrikso.anitube.app.utils;

import android.util.Log;

import androidx.core.util.Pair;

import com.mrikso.anitube.app.model.DubStatusModel;
import com.mrikso.anitube.app.model.EpisodeModel;
import com.mrikso.anitube.app.model.PlayerModel;
import com.mrikso.anitube.app.model.VoicerModel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LinksVideoParser {
    private static final String TAG = "LinksVideoParser";

    public static List<DubStatusModel> getDubStatusModelList(
            List<Pair<String, String>> listDubStatus,
            List<Pair<String, String>> listVoicers,
            List<Pair<String, String>> listPlayers,
            List<EpisodeModel> listAllEpisodes) {
        List<DubStatusModel> dubStatusList = new ArrayList<>();
        for (Pair<String, String> dubStatus : listDubStatus) {

            String id = dubStatus.first;
            String name = dubStatus.second;
            List<Pair<String, String>> newVoicersList =
                    listVoicers.stream()
                            .filter(c -> c.first.startsWith(id))
                            .collect(Collectors.toList());
            List<VoicerModel> voicers =
                    getVoicerModelList(newVoicersList, listPlayers, listAllEpisodes);
            dubStatusList.add(new DubStatusModel(id, name, voicers));
        }
        return dubStatusList;
    }

    public static List<VoicerModel> getVoicerModelList(
            List<Pair<String, String>> listVoicers,
            List<Pair<String, String>> listPlayers,
            List<EpisodeModel> listAllEpisodes) {
        List<VoicerModel> voicersList = new ArrayList<>();
        for (Pair<String, String> voicer : listVoicers) {

            String voicerId = voicer.first;
            String voicerName = voicer.second;
            Log.i(TAG, "dub: " + voicerId + " " + voicerName);

            List<PlayerModel> playerlist =
                    getPlayerModelList(listPlayers, listAllEpisodes, voicerId);
            VoicerModel voicerModel = new VoicerModel(voicerId, voicerName);
            voicerModel.setPlayers(playerlist);
            voicersList.add(voicerModel);
        }
        return voicersList;
    }

    public static List<PlayerModel> getPlayerModelList(
            List<Pair<String, String>> listPlayers,
            List<EpisodeModel> listAllEpisodes,
            String voicerId) {
        List<PlayerModel> playerlist = new ArrayList<>();

        for (Pair<String, String> player : listPlayers) {

            String playerId = player.first;
            String playerName = player.second;
            Log.i(TAG, "player: " + playerId + " " + playerName);
            List<EpisodeModel> episodes =
                    listAllEpisodes.stream()
                            .filter(c -> c.getId().startsWith(playerId))
                            .collect(Collectors.toList());

            if (voicerId == null) {
                playerlist.add(new PlayerModel(playerId, playerName, episodes));
            } else if (playerId.startsWith(voicerId)) {
                playerlist.add(new PlayerModel(playerId, playerName, episodes));
            }
        }
        return playerlist;
    }
}
