package com.mrikso.anitube.app.ui.player;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

//@HiltViewModel
public class PlayerViewModel extends ViewModel {
    private MutableLiveData<Long> playPosition = new MutableLiveData<>(0L);

    public void setPlayPosition(long position){
        playPosition.setValue(position);
    }

    public LiveData<Long> getPlayPosition() {
        return playPosition;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        playPosition.setValue(0L);
    }
}
