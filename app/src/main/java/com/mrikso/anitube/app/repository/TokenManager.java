package com.mrikso.anitube.app.repository;

import android.annotation.SuppressLint;
import android.util.Log;

import com.mrikso.anitube.app.utils.PreferencesHelper;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class TokenManager {
    private final String TAG = "TokenManager";
    private static final long REFRESH_THRESHOLD = TimeUnit.MINUTES.toMillis(5);

    private final PreferencesHelper preferencesHelper;
    private final HikkaRepository hikkaRepository;

    @Inject
    public TokenManager(HikkaRepository hikkaRepository, PreferencesHelper preferencesHelper) {
        this.preferencesHelper = preferencesHelper;
        this.hikkaRepository = hikkaRepository;
        scheduleTokenRefresh();
    }


    public Completable updateToken(long expirationTime) {
        return Completable.fromAction(() -> preferencesHelper.updateHikkaExpirationToken(expirationTime))
                .doOnComplete(() -> {
                    scheduleTokenRefresh();
                })
                .subscribeOn(Schedulers.io());
    }

    public boolean isTokenValid() {
        long expirationTime = preferencesHelper.getHikkaTokenExpirationTime();
        return expirationTime > System.currentTimeMillis();
    }

    @SuppressLint("CheckResult")
    private void scheduleTokenRefresh() {
        Observable.interval(10, TimeUnit.MINUTES)
                .subscribeOn(Schedulers.io())
                .subscribe(tick -> {
                    long expirationTime = preferencesHelper.getHikkaTokenExpirationTime();
                    long currentTime = System.currentTimeMillis();
                    long timeUntilExpiration = expirationTime - currentTime;

                    if (timeUntilExpiration <= REFRESH_THRESHOLD && timeUntilExpiration > 0) {
                        refreshToken().subscribe();
                    } else if (timeUntilExpiration <= 0) {
                        // loadToken();
                    }
                });
    }

    private Completable refreshToken() {
        return hikkaRepository.updateToken()
                // Отримуємо інформацію про токен
                .flatMapCompletable(tokenResponse -> updateToken(tokenResponse.getExpiration()) // Зберігаємо новий токен
                        .doOnComplete(() -> Log.d(TAG, "Token refreshed"))
                        .subscribeOn(Schedulers.io()));
    }
}
