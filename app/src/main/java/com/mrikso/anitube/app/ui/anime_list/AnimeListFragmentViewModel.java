package com.mrikso.anitube.app.ui.anime_list;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelKt;
import androidx.paging.PagingData;
import androidx.paging.rxjava3.PagingRx;

import com.mrikso.anitube.app.App;
import com.mrikso.anitube.app.model.AnimeReleaseModel;
import com.mrikso.anitube.app.model.LoadState;
import com.mrikso.anitube.app.model.UserModel;
import com.mrikso.anitube.app.repository.UserProfileRepository;
import com.mrikso.anitube.app.utils.InternetConnection;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import kotlinx.coroutines.CoroutineScope;

@HiltViewModel
public class AnimeListFragmentViewModel extends ViewModel {
    private final String TAG = "AnimeListFragmentViewModel";
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private final AnimeListRepository repository;
    private final UserProfileRepository userProfileRepository;

    private final MutableLiveData<PagingData<AnimeReleaseModel>> animePagingData = new MutableLiveData<>();
    private final MutableLiveData<UserModel> userData = new MutableLiveData<>(null);
    private final MutableLiveData<LoadState> loadSate = new MutableLiveData<>(LoadState.LOADING);

    @Inject
    public AnimeListFragmentViewModel(AnimeListRepository repository, UserProfileRepository  userProfileRepository) {
        this.repository = repository;
        this.userProfileRepository = userProfileRepository;
        initUserProfileSubscription();
        loadData();
    }

    private void initUserProfileSubscription() {
        compositeDisposable.add(userProfileRepository.getUserModelPublishSubject()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(results -> {
                    if (results != null) {
                        userData.postValue(results);
                    }
                }));
    }

    public void loadData() {
        if (!InternetConnection.isNetworkAvailable(App.getApplication())) {
            loadSate.postValue(LoadState.NO_NETWORK);
            return;
        }
        loadSate.postValue(LoadState.LOADING);
        CoroutineScope viewModelScope = ViewModelKt.getViewModelScope(this);
        Flowable<PagingData<AnimeReleaseModel>> animePagingDataFlowable = PagingRx.cachedIn(repository.getAnimeListByPage(), viewModelScope);

        compositeDisposable.add(animePagingDataFlowable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(animePagingData::setValue));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.dispose();
    }

    public LiveData<PagingData<AnimeReleaseModel>> getAnimePagingData() {
        return animePagingData;
    }

    public LiveData<UserModel> getUserData() {
        return userData;
    }

    public LiveData<LoadState> getLoadState() {
        return loadSate;
    }
}
