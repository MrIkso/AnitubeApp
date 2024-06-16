package com.mrikso.anitube.app.di;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.mrikso.anitube.app.BuildConfig;
import com.mrikso.anitube.app.data.history.HistoryDatabase;
import com.mrikso.anitube.app.data.search.SearchDatabase;
import com.mrikso.anitube.app.network.AddCookiesInterceptor;
import com.mrikso.anitube.app.network.AnitubeApiService;
import com.mrikso.anitube.app.network.ApiClient;
import com.mrikso.anitube.app.network.JsoupConverterFactory;
import com.mrikso.anitube.app.network.UserAgentInterceptor;
import com.mrikso.anitube.app.parser.AnimeReleasesMapper;
import com.mrikso.anitube.app.parser.CollectionsParser;
import com.mrikso.anitube.app.parser.CommentsParser;
import com.mrikso.anitube.app.parser.HomePageParser;
import com.mrikso.anitube.app.repository.AnitubeRepository;
import com.mrikso.anitube.app.ui.anime_list.AnimeListRepository;
import com.mrikso.anitube.app.ui.collections.CollectionsRepository;
import com.mrikso.anitube.app.ui.comments.CommentsRepository;
import com.mrikso.anitube.app.ui.library.LibaryRepository;
import com.mrikso.anitube.app.ui.search.SearchRepository;
import com.mrikso.anitube.app.ui.search_result.SearchResultRepository;
import com.mrikso.anitube.app.ui.watch.WatchAnimeRepository;
import com.mrikso.anitube.app.viewmodel.UserProfileRepository;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import java.util.concurrent.TimeUnit;

import javax.inject.Named;
import javax.inject.Singleton;

import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory;

@Module
@InstallIn(SingletonComponent.class)
public class AppModules {

    @Singleton
    @Provides
    public static HistoryDatabase provideAnimeDatabase(@ApplicationContext Context context) {
        return HistoryDatabase.getInstance(context);
    }

    @Singleton
    @Provides
    public static SearchDatabase provideSearchDatabase(@ApplicationContext Context context) {
        return SearchDatabase.getInstance(context);
    }

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
        return new UserAgentInterceptor(ApiClient.DESKTOP_USER_AGENT);
    }

    @Singleton
    @Provides
    public static AddCookiesInterceptor provideCookiesInterceptor() {
        return new AddCookiesInterceptor();
    }

    @Singleton
    @Provides
    @Named("Normal")
    public static OkHttpClient provideHttpClint(
            HttpLoggingInterceptor httpLoggingInterceptor, UserAgentInterceptor userAgentInterceptor) {
        return new OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(userAgentInterceptor)
                .addInterceptor(httpLoggingInterceptor)
                .followRedirects(true)
                .followSslRedirects(true)
                .build();
    }

    @Singleton
    @Provides
    @Named("Anitube")
    public static OkHttpClient provideAnitubeHttpClint(
            HttpLoggingInterceptor httpLoggingInterceptor,
            UserAgentInterceptor userAgentInterceptor,
            AddCookiesInterceptor cookiesInterceptor) {
        return new OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(userAgentInterceptor)
                .addInterceptor(httpLoggingInterceptor)
                .addInterceptor(cookiesInterceptor)
                .build();
    }

    @Singleton
    @Provides
    public static Retrofit provideRetrofitInstance(@Named("Anitube") OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .baseUrl(ApiClient.BASE_URL)
                .client(okHttpClient)
                .addCallAdapterFactory(RxJava3CallAdapterFactory.createAsync())
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(new JsoupConverterFactory())
                .addConverterFactory(GsonConverterFactory.create())
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
    public static CollectionsRepository provideCollectuonsRepository(AnitubeApiService apiService) {
        return new CollectionsRepository(apiService, new CollectionsParser());
    }

    @Singleton
    @Provides
    public static UserProfileRepository provideUserProfileRepository() {
        return UserProfileRepository.getInstance();
    }

    @Singleton
    @Provides
    public static AnimeListRepository provideAnimeListRepository(
            AnitubeApiService apiService, AnimeReleasesMapper mapper) {
        return new AnimeListRepository(apiService, mapper);
    }

    @Singleton
    @Provides
    public static LibaryRepository provideLibaryRepository(AnitubeApiService apiService, AnimeReleasesMapper mapper) {
        return new LibaryRepository(apiService, mapper);
    }

    @Singleton
    @Provides
    public static SearchRepository provideSearchRepository(
            AnitubeApiService apiService, SearchDatabase searchDatabase, AnimeReleasesMapper mapper) {
        return new SearchRepository(apiService, searchDatabase, mapper);
    }

    @Singleton
    @Provides
    public static WatchAnimeRepository provideWatchRepository(HistoryDatabase database) {
        return new WatchAnimeRepository(database);
    }

    @Singleton
    @Provides
    public static SearchResultRepository provideSearchResultRepository(
            AnitubeApiService apiService, AnimeReleasesMapper mapper) {
        return new SearchResultRepository(apiService, mapper);
    }

    @Singleton
    @Provides
    public static CommentsRepository provideCommentsRepository(AnitubeApiService apiService) {
        return new CommentsRepository(apiService, new CommentsParser());
    }

    @Singleton
    @Provides
    public static SharedPreferences provideSharedPreferences(@ApplicationContext Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Singleton
    @Provides
    public static AnimeReleasesMapper provideAnimeMapper(HomePageParser homePageParser, UserProfileRepository userProfileRepository) {
        return new AnimeReleasesMapper(homePageParser, userProfileRepository);
    }

    @Singleton
    @Provides
    public static HomePageParser provideHomePageParser() {
        return HomePageParser.getInstance();
    }
}
