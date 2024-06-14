package com.mrikso.anitube.app.parser.video;

import android.util.Log;

import androidx.core.util.Pair;

import com.mrikso.anitube.app.parser.video.model.DubStatusModel;
import com.mrikso.anitube.app.parser.video.model.EpisodeModel;
import com.mrikso.anitube.app.parser.video.model.PlayerModel;
import com.mrikso.anitube.app.parser.video.model.VoicerModel;
import com.mrikso.treeview.TreeItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LinksVideoParser {
    private static final String TAG = "LinksVideoParser";
    static String id;

    public static TreeItem<PlayerModel> treeBasedParser(
            Map<String, String> playListMap, List<EpisodeModel> listAllEpisodes) {
        TreeItem<PlayerModel> root = new TreeItem<>();
        Map<String, TreeItem<PlayerModel>> map = new HashMap<>();
        long startTime = System.currentTimeMillis();

        for (Map.Entry<String, String> playList : playListMap.entrySet()) {
            String id = playList.getKey();
            String name = playList.getValue();

            TreeItem<PlayerModel> node = new TreeItem<>(new PlayerModel(id, name));

            map.put(id, node);
            List<EpisodeModel> episodes = listAllEpisodes.stream()
                    .filter(c -> c.getPlayerId().equals(id))
                    .collect(Collectors.toList());
            if (!episodes.isEmpty()) {
                node.getValue().setEpisodes(episodes);
            }
            // робимо дерево
            int lastUnderscore = id.lastIndexOf("_");
            if (lastUnderscore != -1) {
                String parentId = id.substring(0, lastUnderscore);
                // TreeNode parent = map.get(parentId);
                TreeItem<PlayerModel> parent = map.get(parentId);
                if (parent != null) {
                    //Log.i(TAG, "parent add node" + node.toString());
                    parent.addChild(node);

                } else {
                    //Log.i(TAG, "root add node" + node.toString());
                    root.addChild(node);
                }
            }
        }
        long endTime = System.currentTimeMillis();

        //Log.i(TAG, "hat took " + (endTime - startTime) + " milliseconds");
        //Log.i(TAG, root.toString());
        return root;
    }

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
                    listVoicers.stream().filter(c -> c.first.startsWith(id)).collect(Collectors.toList());
            List<VoicerModel> voicers = getVoicerModelList(newVoicersList, listPlayers, listAllEpisodes);
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
            // Log.i(TAG, "dub: " + voicerId + " " + voicerName);

            List<PlayerModel> playerlist = getPlayerModelList(listPlayers, listAllEpisodes, voicerId);
            VoicerModel voicerModel = new VoicerModel(voicerId, voicerName);
            voicerModel.setPlayers(playerlist);
            voicersList.add(voicerModel);
        }
        return voicersList;
    }

    // реліз має лише плеєри та епізоди
    public static List<PlayerModel> getPlayerModelList(
            List<Pair<String, String>> listPlayers, List<EpisodeModel> listAllEpisodes, String voicerId) {
        List<PlayerModel> playerlist = new ArrayList<>();

        for (Pair<String, String> player : listPlayers) {

            String playerId = player.first;
            String playerName = player.second;
            // Log.i(TAG, "player: " + playerId + " " + playerName);
            List<EpisodeModel> episodes = listAllEpisodes.stream()
                    .filter(c -> c.getPlayerId().startsWith(playerId))
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
