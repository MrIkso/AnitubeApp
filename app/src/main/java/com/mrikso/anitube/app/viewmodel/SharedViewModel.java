package com.mrikso.anitube.app.viewmodel;

import androidx.core.util.Pair;
import androidx.lifecycle.ViewModel;

import com.mrikso.anitube.app.model.LoadState;
import com.mrikso.anitube.app.model.VideoLinksModel;
import com.mrikso.anitube.app.parser.DirectVideoUrlParser;

import dagger.hilt.android.lifecycle.HiltViewModel;

import io.reactivex.rxjava3.core.Single;

import okhttp3.OkHttpClient;

import javax.inject.Inject;
import javax.inject.Named;

@HiltViewModel
public class SharedViewModel extends ViewModel {
    private final String TAG = "SharedViewModel";

    private OkHttpClient client;

    @Inject
    public SharedViewModel(@Named("Normal") OkHttpClient client) {
        this.client = client;
    }

    // Init ViewModel Data
    public Single<Pair<LoadState, VideoLinksModel>> loadData(String ifRame) {
        return new DirectVideoUrlParser(client).getDirectUrl(ifRame);
    }
}
