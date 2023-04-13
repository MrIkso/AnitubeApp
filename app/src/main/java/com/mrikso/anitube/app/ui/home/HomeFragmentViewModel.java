package com.mrikso.anitube.app.ui.home;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.mrikso.anitube.app.model.AnimeReleaseModel;
import com.mrikso.anitube.app.model.BaseAnimeModel;
import com.mrikso.anitube.app.model.InteresingModel;
import com.mrikso.anitube.app.model.LoadState;
import com.mrikso.anitube.app.model.SimpleModel;
import com.mrikso.anitube.app.repository.AnitubeRepository;

import dagger.hilt.android.lifecycle.HiltViewModel;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import javax.inject.Inject;

@HiltViewModel
public class HomeFragmentViewModel extends ViewModel {
    private final String TAG = "HomeFragmentViewModel";

    private AnitubeRepository repository;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private MutableLiveData<LoadState> loadSate = new MutableLiveData<>(LoadState.LOADING);
    private MutableLiveData<List<InteresingModel>> _interesingAnime = new MutableLiveData<>(null);
    private MutableLiveData<List<BaseAnimeModel>> _bestAnime = new MutableLiveData<>(null);

    private MutableLiveData<List<AnimeReleaseModel>> _newAnime = new MutableLiveData<>(null);

    @Inject
    public HomeFragmentViewModel(AnitubeRepository repository) {
        this.repository = repository;
    }

    public void loadAnimeBySearchingRequest(String searchingRequest) {
        /*
              Disposable disposable =
                      ApiFactory.apiService
                              .loadAnimeBySearchingRequest(searchingRequest)
                              .subscribeOn(Schedulers.io())
                              .doOnSubscribe(
                                      new Consumer<Disposable>() {
                                          @Override
                                          public void accept(Disposable disposable) throws Throwable {
                                              isNoSearchingResult.setValue(false);
                                          }
                                      })
                              .observeOn(AndroidSchedulers.mainThread())
                              .subscribe(
                                      new Consumer<AnimeResponse>() {
                                          @Override
                                          public void accept(AnimeResponse animeResponse)
                                                  throws Throwable {
                                              if (animeResponse.getAnimes().isEmpty()) {
                                                  isNoSearchingResult.setValue(true);
                                                  animes.setValue(new ArrayList<>());
                                              } else {
                                                  animes.setValue(animeResponse.getAnimes());
                                              }
                                          }
                                      },
                                      new Consumer<Throwable>() {
                                          @Override
                                          public void accept(Throwable throwable) throws Throwable {
                                              Log.d(TAG, throwable.toString());
                                          }
                                      }
            );
              compositeDisposable.add(disposable);
        */
    }

    public void loadHome() {

        Disposable disposable =
                repository
                        .getHome()
                        .subscribeOn(Schedulers.io())
                        .doOnSubscribe(
                                new Consumer<Disposable>() {
                                    @Override
                                    public void accept(Disposable disposable) throws Throwable {
                                        loadSate.setValue(LoadState.LOADING);
                                        Log.d(TAG, "start loading");
                                    }
                                })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                new Consumer<Document>() {
                                    @Override
                                    public void accept(Document response) throws Throwable {
                                        Executors.newSingleThreadExecutor()
                                                .execute(
                                                        () -> {
                                                            parseHomePage(response);
                                                        });
                                    }
                                },
                                new Consumer<Throwable>() {
                                    @Override
                                    public void accept(Throwable throwable) throws Throwable {
                                        Log.d(TAG, throwable.toString());
                                        //	isDataLoading.setValue(false);
                                        loadSate.setValue(LoadState.ERROR);
                                    }
                                });

        compositeDisposable.add(disposable);
    }

    private void parseHomePage(Document home) {
        List<InteresingModel> interestingAnimeList = new ArrayList<>();
        List<BaseAnimeModel> bestAnimeList = new ArrayList<>();
        List<AnimeReleaseModel> releasesAnimeList = new ArrayList<>();
        Elements slideBody =
                home.getElementById("header_img")
                        .select("div.slide_block")
                        .select("div.slide_body");
        Elements sliders = slideBody.first().getElementsByTag("a");

        for (Element slider : sliders) {
            String url = slider.attr("href");
            String imageUrl = slider.getElementsByTag("img").first().attr("src");
            // Log.d(TAG, url + "=>" + imageUrl);
            InteresingModel interesting = new InteresingModel(imageUrl, url);
            interestingAnimeList.add(interesting);
        }

        Elements contentElement = home.select("div.content");
        Elements bestAnimes =
                contentElement
                        .select("div.box")
                        .first()
                        .select("div.example")
                        .select("div.carousel")
                        .select("div.carousel_container")
                        .select("ul.portfolio_items")
                        .select("li");

        for (Element beastAnime : bestAnimes) {
            Element posterAnimeElement = beastAnime.selectFirst("div.sl_poster");
            String urlPoster = posterAnimeElement.getElementsByTag("a").attr("href");
            String imgUrl = posterAnimeElement.getElementsByTag("img").attr("src");
            String name = beastAnime.selectFirst("div.text_content").getElementsByTag("a").text();
            // Log.d(TAG, urlPoster + " " + imgUrl + " " + name);
            bestAnimeList.add(new BaseAnimeModel(name, imgUrl, urlPoster));
        }

        Elements newsAnimes = contentElement.select("div.news_2");
        //	Log.d(TAG, newsAnimes.html());
        for (Element newAmime : newsAnimes) {
            Element title = newAmime.select("div.title2").first();
            if (title != null) {
                Element title_a = title.getElementsByTag("a").first();

                String animeUrl = title_a.attr("href");
                String animeTitle = title_a.text();
                String urlPoster =
                        newAmime.selectFirst("div.news_2_c_l")
                                .getElementsByTag("img")
                                .first()
                                .attr("src");

                Element infaBase = newAmime.selectFirst("div.news_2_c_r");
                Element infa = infaBase.selectFirst("div.news_2_c_inf");

                // витягування року випуску аніме
                Element yearElement = infa.selectFirst("div.news_2_infa dt:contains(Рік) + a");
                SimpleModel releaseYear = new SimpleModel(yearElement.text().trim(), null);

                // Отримання кількості серій та їх тривалості
                String episodes =
                        infa.select("div.news_2_infa dt:contains(Серій:)")
                                .first()
                                .nextSibling()
                                .toString()
                                .trim();

                Element newsElement = infaBase.selectFirst(".news_2_c_text");
                String description = newsElement.text().replace("Опис:", "").trim();

                AnimeReleaseModel animeRelease = new AnimeReleaseModel();
                animeRelease.setTitle(animeTitle);
                animeRelease.setPosterUrl(urlPoster);
                animeRelease.setAnimeUrl(animeUrl);
                animeRelease.setEpisodes(episodes);
                animeRelease.setReleaseYear(releaseYear);
                animeRelease.setDescription(description);

                releasesAnimeList.add(animeRelease);
                Log.d(TAG, animeRelease.toString());
            }
        }

        _interesingAnime.postValue(interestingAnimeList);
        _bestAnime.postValue(bestAnimeList);
        _newAnime.postValue(releasesAnimeList);
        loadSate.postValue(LoadState.DONE);
    }

    public LiveData<LoadState> getLoadState() {
        return loadSate;
    }

    public LiveData<List<InteresingModel>> getInteresingAnime() {
        return _interesingAnime;
    }

    public LiveData<List<BaseAnimeModel>> getBestAnime() {
        return _bestAnime;
    }

    public LiveData<List<AnimeReleaseModel>> getNewAnime() {
        return _newAnime;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.dispose();
    }
}
