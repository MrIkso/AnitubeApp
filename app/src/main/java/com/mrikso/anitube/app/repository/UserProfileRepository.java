package com.mrikso.anitube.app.repository;

import com.mrikso.anitube.app.model.UserModel;

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
        if (userModel != null) {
            userModelPublishSubject.onNext(userModel);
        }
    }
}
