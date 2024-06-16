package com.mrikso.anitube.app.viewmodel;

import com.mrikso.anitube.app.model.UserModel;
import com.mrikso.anitube.app.parser.video.model.EpisodeModel;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;

public class UserProfileRepository {
    private static UserProfileRepository instance;

    private final PublishSubject<UserModel> userModelPublishSubject;

    public static synchronized UserProfileRepository getInstance() {
        if (instance == null) {
            instance = new UserProfileRepository();
        }
        return instance;
    }

    public UserProfileRepository() {
        userModelPublishSubject = PublishSubject.create();
    }

    public Observable<UserModel> getUserModelPublishSubject() {
        return userModelPublishSubject;
    }

    public void setUserModel(@Nullable UserModel userModel){
        userModelPublishSubject.onNext(userModel);
    }
}
