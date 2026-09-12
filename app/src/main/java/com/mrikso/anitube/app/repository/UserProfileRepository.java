package com.mrikso.anitube.app.repository;

import com.mrikso.anitube.app.model.UserModel;

import javax.annotation.Nullable;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

public class UserProfileRepository {
    private static UserProfileRepository instance;

    private final BehaviorSubject<UserWrapper> userModelSubject;

    public static synchronized UserProfileRepository getInstance() {
        if (instance == null) {
            instance = new UserProfileRepository();
        }
        return instance;
    }

    public UserProfileRepository() {
        userModelSubject = BehaviorSubject.createDefault(new UserWrapper(null));
    }

    public Observable<UserModel> getUserModelPublishSubject() {
        return userModelSubject.map(wrapper -> wrapper.user != null ? wrapper.user : new UserModel("", "", ""));
    }

    public void setUserModel(@Nullable UserModel userModel){
        userModelSubject.onNext(new UserWrapper(userModel));
    }

    public void clearUserModel() {
        userModelSubject.onNext(new UserWrapper(null));
    }

    public static class UserWrapper {
        @Nullable
        public final UserModel user;

        public UserWrapper(@Nullable UserModel user) {
            this.user = user;
        }
    }
}
