package com.mrikso.anitube.app.ui.collections;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelKt;
import androidx.paging.PagingData;
import androidx.paging.rxjava3.PagingRx;

import com.mrikso.anitube.app.App;
import com.mrikso.anitube.app.model.CollectionModel;
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
public class CollectionsFragmentViewModel extends ViewModel {
    private final String TAG = "CollectionsFragmentViewModel";
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private CollectionsRepository repository;
    private final UserProfileRepository userProfileRepository;

    private MutableLiveData<PagingData<CollectionModel>> collectionPagingData = new MutableLiveData<>();
    private Flowable<PagingData<CollectionModel>> collectionPagingDataFlowable;
    private final MutableLiveData<UserModel> userData = new MutableLiveData<>(null);
    private final MutableLiveData<LoadState> loadSate = new MutableLiveData<>(LoadState.LOADING);

    @Inject
    public CollectionsFragmentViewModel(CollectionsRepository repository, UserProfileRepository userProfileRepository) {
        this.repository = repository;
        this.userProfileRepository = userProfileRepository;
        initUserProfileSubscription();
        loadData();
    }

    private void initUserProfileSubscription() {
        compositeDisposable.add(userProfileRepository.getUserModelPublishSubject()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(userData::postValue));
    }

    public void loadData() {
        if (!InternetConnection.isNetworkAvailable(App.getApplication())) {
            loadSate.postValue(LoadState.NO_NETWORK);
            return;
        }
        loadSate.postValue(LoadState.LOADING);
        CoroutineScope viewModelScope = ViewModelKt.getViewModelScope(this);
        collectionPagingDataFlowable = PagingRx.cachedIn(repository.getCollections(), viewModelScope);

        compositeDisposable.add(collectionPagingDataFlowable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pagingData -> {
                    collectionPagingData.setValue(pagingData);
                }));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.dispose();
    }

    public LiveData<PagingData<CollectionModel>> getCollectionPagingData() {
        return collectionPagingData;
    }

    public LiveData<LoadState> getLoadState() {
        return loadSate;
    }

    public LiveData<UserModel> getUserData() {
        return userData;
    }
}
