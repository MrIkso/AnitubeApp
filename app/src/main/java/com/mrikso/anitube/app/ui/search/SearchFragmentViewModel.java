package com.mrikso.anitube.app.ui.search;

import androidx.constraintlayout.widget.ReactiveGuide;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import dagger.hilt.android.lifecycle.HiltViewModel;
import kotlin.collections.UArraySortingKt;

// @HiltViewModel
public class SearchFragmentViewModel extends ViewModel {
    MutableLiveData<Integer> couterData = new MutableLiveData<>();
    int count = 0;

    public void count() {

        couterData.setValue(count ++);
    }

    public LiveData<Integer> getCounterData() {
        return couterData;
    }
}
