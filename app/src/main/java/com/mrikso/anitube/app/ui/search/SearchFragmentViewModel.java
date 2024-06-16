package com.mrikso.anitube.app.ui.search;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelKt;
import androidx.paging.PagingData;
import androidx.paging.rxjava3.PagingRx;

import com.mrikso.anitube.app.data.search.RecentSearch;
import com.mrikso.anitube.app.model.AnimeReleaseModel;
import com.mrikso.anitube.app.model.SimpleModel;

import dagger.hilt.android.lifecycle.HiltViewModel;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;

import java.util.List;

import javax.inject.Inject;

import kotlinx.coroutines.CoroutineScope;

@HiltViewModel
public class SearchFragmentViewModel extends ViewModel {
    private final String TAG = "SearchFragmentViewModel";
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private LiveData<List<RecentSearch>> searchData = new MutableLiveData<>();
    private final MutableLiveData<List<SimpleModel>> quickSearchResult = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _showSearchResultAdapter = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> _showRecentSearchResultScreen = new MutableLiveData<>(false);
    private final MutableLiveData<PagingData<AnimeReleaseModel>> animePagingData = new MutableLiveData<>();
    private final SearchRepository searchRepository;

    @Inject
    public SearchFragmentViewModel(SearchRepository searchRepository) {
        this.searchRepository = searchRepository;
        loadRecentSearches();
    }

    public void loadRecentSearches() {
        searchData = searchRepository.getRecentSearch();
    }

    public void runQuickSearch(String query, String dleHash) {
        Disposable disposable = searchRepository
                .runQickSearch(query, dleHash)
                .subscribe(
                        new Consumer<List<SimpleModel>>() {
                            @Override
                            public void accept(List<SimpleModel> result) throws Throwable {
                                quickSearchResult.postValue(result);
                            }
                        },
                        new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Throwable {
                                throwable.printStackTrace();
                                /// loadSate.postValue(LoadState.ERROR);
                                ///  errorMessage.postValue(throwable.getMessage());
                            }
                        });

        compositeDisposable.add(disposable);
    }

    public void addRecentSearch(String query) {
        searchRepository.addRecentSearch(query);
    }

    public void removeRecentSearch(String query) {
        searchRepository.removeRecentSearch(query);
    }

    public void clearAllRecentSearch() {
        searchRepository.clearAllRecentSearch();
    }

    public void getSearchResult(String query) {
        CoroutineScope viewModelScope = ViewModelKt.getViewModelScope(this);
        Flowable<PagingData<AnimeReleaseModel>> animePagingDataFlowable = PagingRx.cachedIn(searchRepository.getSearchResult(query), viewModelScope);

        compositeDisposable.add(animePagingDataFlowable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(animePagingData::postValue));
    }

    public LiveData<List<RecentSearch>> getSearchHistoryData() {
        return searchData;
    }

    public LiveData<List<SimpleModel>> getQuickSearchResult() {
        return quickSearchResult;
    }

    public LiveData<Boolean> isShowSearchResultAdapter() {
        return _showSearchResultAdapter;
    }

    public LiveData<Boolean> isShowRecentSearchResultScreen() {
        return _showRecentSearchResultScreen;
    }

    public LiveData<PagingData<AnimeReleaseModel>> getAnimePagingData() {
        return animePagingData;
    }

    public void showSearchResultAdapter() {
        _showSearchResultAdapter.setValue(true);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.dispose();
    }

    public void showRecentSearchResultEmptyScreen() {
        _showRecentSearchResultScreen.postValue(true);
    }
}
