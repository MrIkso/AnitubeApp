package com.mrikso.anitube.app.di;

import com.mrikso.anitube.app.BuildConfig;
import com.mrikso.anitube.app.network.AnitubeApiService;
import com.mrikso.anitube.app.network.ApiClient;
import com.mrikso.anitube.app.network.JsoupConverterFactory;
import com.mrikso.anitube.app.network.UserAgentInterceptor;
import com.mrikso.anitube.app.repository.AnitubeRepository;

import com.mrikso.anitube.app.ui.anime_list.AnimeListRepository;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory;

@Module
@InstallIn(SingletonComponent.class)
public class NetworkModule {

    @Singleton
    @Provides
    public static HttpLoggingInterceptor provideHttpLoggerInterceptor() {
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        if (BuildConfig.DEBUG) {
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
        } else {
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.NONE);
        }
        return httpLoggingInterceptor;
    }

    @Singleton
    @Provides
    public static UserAgentInterceptor provideUserAgentInterceptor() {
        return new UserAgentInterceptor(
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko)"
                        + " Chrome/86.0.4240.198 Safari/537.36");
    }

    @Singleton
    @Provides
    public static OkHttpClient provideHttpClint(
            HttpLoggingInterceptor httpLoggingInterceptor,
            UserAgentInterceptor userAgentInterceptor) {
        return new OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(userAgentInterceptor)
                .addInterceptor(httpLoggingInterceptor)
                .build();
    }

    //    @Singleton
    //    @Provides
    //    public static GsonConverterFactory provideGsonConverterFactory() {
    //        return GsonConverterFactory.create();
    //    }

    @Singleton
    @Provides
    public static JsoupConverterFactory provideJsoupConverterFactory() {
        return new JsoupConverterFactory();
    }

    @Singleton
    @Provides
    public static ScalarsConverterFactory provideScalarsConverterFactory() {
        return ScalarsConverterFactory.create();
    }

    @Singleton
    @Provides
    public static Retrofit provideRetrofitInstance(
            OkHttpClient okHttpClient,
            ScalarsConverterFactory scalarsConverterFactory,
            JsoupConverterFactory jsoupConverterFactory) {
        return new Retrofit.Builder()
                .baseUrl(ApiClient.BASE_URL)
                .client(okHttpClient)
                .addCallAdapterFactory(RxJava3CallAdapterFactory.createAsync())
                // .addConverterFactory(gsonConverterFactory)
                .addConverterFactory(scalarsConverterFactory)
                .addConverterFactory(jsoupConverterFactory)
                .build();
    }

    @Singleton
    @Provides
    public static AnitubeApiService provideApiFactory(Retrofit retrofit) {
        return retrofit.create(AnitubeApiService.class);
    }

    @Singleton
    @Provides
    public static AnitubeRepository provideAnitubeRepository(AnitubeApiService apiService) {
        return new AnitubeRepository(apiService);
    }

    @Singleton
    @Provides
    public static AnimeListRepository provideAnimeListRepository(AnitubeApiService apiService) {
        return new AnimeListRepository(apiService);
    }
}
