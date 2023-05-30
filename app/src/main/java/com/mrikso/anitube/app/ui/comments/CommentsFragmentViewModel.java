package com.mrikso.anitube.app.ui.comments;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelKt;
import androidx.paging.PagingData;
import androidx.paging.rxjava3.PagingRx;

import com.mrikso.anitube.app.model.CommentModel;

import dagger.hilt.android.lifecycle.HiltViewModel;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import javax.inject.Inject;

import kotlinx.coroutines.CoroutineScope;

@HiltViewModel
public class CommentsFragmentViewModel extends ViewModel {
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private CommentsRepository repository;
    private MutableLiveData<PagingData<CommentModel>> commentsPagingData = new MutableLiveData<>();

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

    public LiveData<PagingData<CommentModel>> getCommentsPagingData() {
        return commentsPagingData;
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
}
