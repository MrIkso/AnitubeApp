package com.mrikso.anitube.app.viewmodel;

import com.mrikso.anitube.app.parser.video.model.EpisodeModel;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;

import java.util.ArrayList;
import java.util.List;

public class ListRepository {

    private static ListRepository instance;
    private List<EpisodeModel> currentList = new ArrayList<>();
    private final PublishSubject<List<EpisodeModel>> subject;

    public static synchronized ListRepository getInstance() {
        if (instance == null) {
            instance = new ListRepository();
        }
        return instance;
    }

    public ListRepository() {
        subject = PublishSubject.create();
    }

    public synchronized void setEpisodes(List<EpisodeModel> episodes) {
        currentList = episodes;
        subject.onNext(currentList);
    }

    public synchronized void addItem(EpisodeModel item) {
        currentList.add(item);
        subject.onNext(currentList);
    }

    public synchronized void updateItem(EpisodeModel item, int position) {
        List<EpisodeModel> newList = new ArrayList<>(currentList);
        newList.set(position, item);
        subject.onNext(newList);
    }

    public Observable<List<EpisodeModel>> getData() {
        return subject;
    }

    public synchronized List<EpisodeModel> getList() {
        return currentList;
    }
}
