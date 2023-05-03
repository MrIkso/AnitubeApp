package com.mrikso.anitube.app.ui.watch;

import android.util.Log;

import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.gson.Gson;
import com.mrikso.anitube.app.model.AnimeDetailsModel;
import com.mrikso.anitube.app.model.LoadState;
import com.mrikso.anitube.app.model.ResponseModel;
import com.mrikso.anitube.app.parser.video.LinksVideoParser;
import com.mrikso.anitube.app.parser.video.ParseVideosFromPage;
import com.mrikso.anitube.app.parser.video.model.EpisodeModel;
import com.mrikso.anitube.app.parser.video.model.PlayerModel;
import com.mrikso.anitube.app.repository.AnitubeRepository;
import com.mrikso.anitube.app.utils.FileCache;
import com.mrikso.treeview.TreeItem;

import dagger.hilt.android.lifecycle.HiltViewModel;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import javax.inject.Inject;

@HiltViewModel
public class WatchAnimeFragmentViewModel extends ViewModel {
    private final String TAG = "WatchAnimeFragmentViewModel";

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private AnitubeRepository repository;
    private final MutableLiveData<LoadState> loadSate = new MutableLiveData<>(LoadState.LOADING);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<AnimeDetailsModel> detailsModel = new MutableLiveData<>();
    private final MutableLiveData<TreeItem<PlayerModel>> playlistTree = new MutableLiveData<>();
    //   private final MutableLiveData<List<VoicerModel>> voicerModel = new MutableLiveData<>();
    // private final MutableLiveData<List<DubStatusModel>> dubStatusModel = new MutableLiveData<>();
    //  private final MutableLiveData<List<PlayerModel>> playerModel = new MutableLiveData<>();
    //  private final MutableLiveData<Pair<LoadState, String>> directVideoUrl = new
    // MutableLiveData<>();
    private boolean singleLoad = false;

    @Inject
    public WatchAnimeFragmentViewModel(AnitubeRepository repository) {
        this.repository = repository;
    }

    public void loadPlaylist(boolean isHavePlaylistsAjax, int id, String url) {
        if (!singleLoad) {
            loadData(isHavePlaylistsAjax, id, url);
        }
        singleLoad = true;
    }

    public void reloadPlaylist(boolean isHavePlaylistsAjax, int id, String url) {
        loadData(isHavePlaylistsAjax, id, url);
    }

    private void loadData(boolean isHavePlaylistsAjax, int id, String url) {
        if (isHavePlaylistsAjax) {
            Disposable disposable =
                    repository
                            .getPlaylist(id)
                            .subscribeOn(Schedulers.io())
                            .doOnSubscribe(
                                    new Consumer<Disposable>() {
                                        @Override
                                        public void accept(Disposable disposable) throws Throwable {
                                            loadSate.postValue(LoadState.LOADING);
                                            Log.d(TAG, "start loading");
                                        }
                                    })
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    new Consumer<String>() {
                                        @Override
                                        public void accept(String response) throws Throwable {
                                            Executors.newSingleThreadExecutor()
                                                    .execute(
                                                            () -> {
                                                                loadPlaylistFromAjax(response);
                                                            });
                                        }
                                    },
                                    new Consumer<Throwable>() {
                                        @Override
                                        public void accept(Throwable throwable) throws Throwable {
                                            Log.d(TAG, throwable.toString());
                                            loadSate.postValue(LoadState.ERROR);
                                            errorMessage.postValue(throwable.getMessage());
                                        }
                                    });

            compositeDisposable.add(disposable);
        } else {
            loadVideosFromPage();
        }
    }

    public LiveData<LoadState> getLoadState() {
        return loadSate;
    }

    //    public LiveData<List<DubStatusModel>> getDubStatusPlayList() {
    //        return dubStatusModel;
    //    }
    //
    //    public LiveData<List<VoicerModel>> getVoicersPlayList() {
    //        return voicerModel;
    //    }
    //
    //    public LiveData<List<PlayerModel>> getPlayersPlayList() {
    //        return playerModel;
    //    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<TreeItem<PlayerModel>> getPlaylistTree() {
        return playlistTree;
    }

    private void loadPlaylistFromAjax(String response) {

        // Log.d(TAG, response);
        try {
            ResponseModel responseModel = new Gson().fromJson(response, ResponseModel.class);
            if (responseModel.isSuccess()) {
                Document doc = Jsoup.parse(responseModel.getResponse());
                List<Pair<String, String>> listDubStatus = new ArrayList<>();
                List<Pair<String, String>> listVoicers = new ArrayList<>();
                List<Pair<String, String>> listPlayers = new ArrayList<>();

                // парсимо всі плейлисти
                Elements playlists = doc.select(".playlists-lists .playlists-items li");
                Map<String, String> treeMap = new LinkedHashMap<>();
                Log.i(TAG, "tree parser");
                for (Element playlistElement : playlists) {
                    String id = playlistElement.attr("data-id");
                    String name = playlistElement.text();
                    Log.i(TAG, "id: " + id + " name:" + name);
                    treeMap.put(id, name);

                    /*
                                      int type = ParserUtils.countMatches(id, "_");
                                      if (type == 1) {
                                          listDubStatus.add(new Pair<>(id, name));
                                      } else if (type == 2) {
                                          listVoicers.add(new Pair<>(id, name));
                                      } else if (type == 3) {
                                          listPlayers.add(new Pair<>(id, name));
                                      }
                    */
                }
                /*
                            // реліє має вибір субтитрів і озвучку різних дабберів і плеєра
                            // перший елемент ОЗВУЧУВАННЯ другий СУБТИТРИ
                            if (playlists.first().text().equalsIgnoreCase("ОЗВУЧУВАННЯ")) {
                                Log.i(TAG, "subs detected");
                                for (Element playlistElement : playlists) {
                                    String id = playlistElement.attr("data-id");
                                    String name = playlistElement.text();
                                    //Log.i(TAG, "id: " + id + " name:" + name);
                                    int type = ParserUtils.countMatches(id, "_");
                                    if (type == 1) {
                                        listDubStatus.add(new Pair<>(id, name));
                                    } else if (type == 2) {
                                        listVoicers.add(new Pair<>(id, name));
                                    } else if (type == 3) {
                                        listPlayers.add(new Pair<>(id, name));
                                    }
                                }
                            }
                            // реліз має лише плеєри, без субтитрів і вибору дабберів і плеєєра
                            else if (playlists.first().text().startsWith("ПЛЕЄР")) {
                                Log.i(TAG, "only payers mode");
                                for (Element playlistElement : playlists) {
                                    String id = playlistElement.attr("data-id");
                                    String name = playlistElement.text();
                                    //Log.i(TAG, "id: " + id + " name:" + name);
                                    int type = ParserUtils.countMatches(id, "_");
                                    if (type == 1) {
                                        listPlayers.add(new Pair<>(id, name));
                                    }
                                }
                            } else {
                                // реліз має вибір даббера і плееєра
                                // буває порядок змінюється і спочатку ідуть дабери а потім вибір типу озвучка
                                // чи субтитри і далі сам плеєр
                                Log.i(TAG, "normal mode");
                                for (Element playlistElement : playlists) {
                                    String id = playlistElement.attr("data-id");
                                    String name = playlistElement.text();
                                    //Log.i(TAG, "id: " + id + " name:" + name);
                                    int type = ParserUtils.countMatches(id, "_");
                                    if (type == 1) {
                                        listVoicers.add(new Pair<>(id, name));
                                    }else if(name.startsWith("ОЗВУЧЕННЯ") ||name.startsWith("ОЗВУЧУВАННЯ") ||name.startsWith("СУБТИТРИ") ){
                                listDubStatus.add(new Pair<>(id, name));
                            }
                         else if (name.startsWith("ПЛЕЄР")) {
                                        listPlayers.add(new Pair<>(id, name));
                                    }
                                }
                            }
                "*/
                List<EpisodeModel> allEpisodesList = new ArrayList<>();

                // парсимо всі епізоди
                Elements episodeElements = doc.select(".playlists-videos .playlists-items li");
                for (Element episodeElement : episodeElements) {
                    String audioId = episodeElement.attr("data-id"); // 0_0_0 or 0_0_0_0 if subs
                    String episodeId = episodeElement.text();
                    String url = episodeElement.attr("data-file");
                    Log.i(TAG, "id: " + audioId + " url: " + url);
                    EpisodeModel episodeModel = new EpisodeModel(audioId, episodeId, url);
                    allEpisodesList.add(episodeModel);
                }

                playlistTree.postValue(LinksVideoParser.treeBasedParser(treeMap, allEpisodesList));
                loadSate.postValue(LoadState.DONE);
                // mapToModel(listDubStatus, listVoicers, listPlayers, allEpisodesList);

            } else {
                loadSate.postValue(LoadState.ERROR);
                errorMessage.postValue(responseModel.getMessage());
            }

        } catch (Exception err) {
            err.printStackTrace();
            loadSate.postValue(LoadState.ERROR);
            errorMessage.postValue(err.getMessage());
        }
    }
    /*
       private void mapToModel(
               List<Pair<String, String>> listDubStatus,
               List<Pair<String, String>> listVoicers,
               List<Pair<String, String>> listPlayers,
               List<EpisodeModel> listAllEpisodes) {
           if (!listDubStatus.isEmpty()) {
               List<DubStatusModel> dubStatusList = new ArrayList<>();
               dubStatusList =
                       LinksVideoParser.getDubStatusModelList(
                               listDubStatus, listVoicers, listPlayers, listAllEpisodes);
               dubStatusModel.postValue(dubStatusList);
               Gson gson = new GsonBuilder().setPrettyPrinting().create();
             //  Log.i(TAG, " "+gson.toJson(dubStatusList));

           } else if (listDubStatus.isEmpty() && listVoicers.isEmpty()) {
               List<PlayerModel> playerList = new ArrayList<>();
               playerList = LinksVideoParser.getPlayerModelList(listPlayers, listAllEpisodes, null);
               Gson gson = new GsonBuilder().setPrettyPrinting().create();
              // Log.i(TAG, " "+gson.toJson(playerList));
               playerModel.postValue(playerList);
           } else {
               List<VoicerModel> voicersList = new ArrayList<>();
               voicersList =
                       LinksVideoParser.getVoicerModelList(listVoicers, listPlayers, listAllEpisodes);
               voicerModel.postValue(voicersList);
               Gson gson = new GsonBuilder().setPrettyPrinting().create();
               //Log.i(TAG, " "+gson.toJson(voicersList));
           }

           loadSate.postValue(LoadState.DONE);
       }
    */

    private void loadVideosFromPage() {
        Executors.newSingleThreadExecutor()
                .execute(
                        () -> {
                            try {
                                Document doc = Jsoup.parse(FileCache.readPage());
                                parseVideosFromPage(doc);
                            } catch (Exception err) {
                                err.printStackTrace();
                                loadSate.postValue(LoadState.ERROR);
                                errorMessage.postValue(err.getMessage());
                            }
                        });
    }

    private void parseVideosFromPage(Document response) {
        ParseVideosFromPage parse = new ParseVideosFromPage(response);
        compositeDisposable.add(
                parse.getPlayerTree()
                        .subscribeOn(Schedulers.io())
                        .subscribe(
                                v -> {
                                    playlistTree.postValue(v);
                                    loadSate.postValue(LoadState.DONE);
                                }));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.dispose();
    }
}
