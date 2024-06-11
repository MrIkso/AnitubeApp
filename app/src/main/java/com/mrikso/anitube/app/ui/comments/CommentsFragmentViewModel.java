package com.mrikso.anitube.app.ui.comments;

import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelKt;
import androidx.paging.PagingData;
import androidx.paging.rxjava3.PagingRx;

import com.google.common.base.Strings;
import com.mrikso.anitube.app.model.CommentModel;
import com.mrikso.anitube.app.model.LoadState;
import com.mrikso.anitube.app.parser.CommentsParser;
import com.mrikso.anitube.app.utils.PreferencesHelper;

import org.jsoup.nodes.Document;

import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import kotlinx.coroutines.CoroutineScope;

@HiltViewModel
public class CommentsFragmentViewModel extends ViewModel {
    private final String TAG = "CommentsFragmentViewModel";
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final CommentsRepository repository;
    private final MutableLiveData<PagingData<CommentModel>> commentsPagingData = new MutableLiveData<>();
    private final MutableLiveData<Pair<LoadState, String>> loadSate = new MutableLiveData<>(null);
    private boolean singleLoad = false;

    @Inject
    public CommentsFragmentViewModel(CommentsRepository repository) {
        this.repository = repository;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.dispose();
    }

    public void loadComments(int animeId) {
        if (!singleLoad) {
            CoroutineScope viewModelScope = ViewModelKt.getViewModelScope(this);
            Flowable<PagingData<CommentModel>> commentsPagingDataFlowable =
                    PagingRx.cachedIn(repository.loadComments(animeId), viewModelScope);

            compositeDisposable.add(commentsPagingDataFlowable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(commentsPagingData::setValue));
            singleLoad = true;
        }
    }

    public void addComments(int animeId, String comment) {
        PreferencesHelper helper = PreferencesHelper.getInstance();

        compositeDisposable.add(repository.addComment(animeId, comment, helper.getUserLogin(), helper.getDleHash())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(v -> {
                    loadSate.setValue(new Pair<>(LoadState.LOADING, null));
                })
                .subscribe(
                        this::parseAddCommentResponse,
                        throwable -> {
                            throwable.printStackTrace();
                            loadSate.postValue(new Pair<>(LoadState.ERROR, throwable.getMessage()));
                        }));
    }

    public LiveData<PagingData<CommentModel>> getCommentsPagingData() {
        return commentsPagingData;
    }

    public LiveData<Pair<LoadState, String>> getLoadState() {
        return loadSate;
    }

    private void parseAddCommentResponse(Document response){
        Executors.newSingleThreadExecutor().execute(() -> {
            String message = new CommentsParser().getAddCommentResponse(response);
            if (Strings.isNullOrEmpty(message)) {
                loadSate.postValue(new Pair<>(LoadState.DONE, null));
            } else {
                loadSate.postValue(new Pair<>(LoadState.ERROR, message));
            }

        });
    }
}

