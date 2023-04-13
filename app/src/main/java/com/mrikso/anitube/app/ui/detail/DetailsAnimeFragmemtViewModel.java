package com.mrikso.anitube.app.ui.detail;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mrikso.anitube.app.model.AnimeDetailsModel;
import com.mrikso.anitube.app.model.BaseAnimeModel;
import com.mrikso.anitube.app.model.DubbersTeam;
import com.mrikso.anitube.app.model.LoadState;
import com.mrikso.anitube.app.model.ScreenshotModel;
import com.mrikso.anitube.app.model.SimpleModel;
import com.mrikso.anitube.app.repository.AnitubeRepository;
import com.mrikso.anitube.app.utils.ParserUtils;

import dagger.hilt.android.lifecycle.HiltViewModel;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.observers.DisposableSingleObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;

import org.jsoup.internal.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

@HiltViewModel
public class DetailsAnimeFragmemtViewModel extends ViewModel {
    private final String TAG = "DetailsAnimeFragmemtViewModel";

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private AnitubeRepository repository;
    private MutableLiveData<LoadState> loadSate = new MutableLiveData<>(LoadState.LOADING);
    private MutableLiveData<AnimeDetailsModel> detailsModel;

    @Inject
    public DetailsAnimeFragmemtViewModel(AnitubeRepository repository) {
        this.repository = repository;
    }

    public void loadData(String url) {
        Log.d(TAG, "loadData called");
        if (detailsModel == null) {
            detailsModel = new MutableLiveData<>();
            loadAnime(url);
        }
    }
	
	public void loadAnime(String url){
		Disposable disposable =
                    repository
                            .getAnimePage(url)
                            .subscribeOn(Schedulers.io())
                            .doOnSubscribe(
                                    new Consumer<Disposable>() {
                                        @Override
                                        public void accept(Disposable disposable) throws Throwable {
                                            loadSate.postValue(LoadState.LOADING);
                                            Log.d(TAG, "start loading");
                                        }
                                    })
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeWith(
                                    new DisposableSingleObserver<Document>() {
                                        @Override
                                        public void onSuccess(Document response) {
                                            Executors.newSingleThreadExecutor()
                                                    .execute(
                                                            () -> {
                                                                parseAnimePage(response);
                                                            });
                                        }

                                        @Override
                                        public void onError(Throwable throwable) {
                                            throwable.printStackTrace();
                                            loadSate.postValue(LoadState.ERROR);
                                        }
                                    });

            compositeDisposable.add(disposable);
	}

    public LiveData<LoadState> getLoadState() {
        return loadSate;
    }

    public LiveData<AnimeDetailsModel> getDetails() {
        return detailsModel;
    }

    private void parseAnimePage(Document doc) {

        AnimeDetailsModel animeDetailsModel = new AnimeDetailsModel();

        Element storyElement = doc.selectFirst("#dle-content > article.story");
        int id = -1;
        // #dle-content > article > div:nth-child(3) > div
        Element idElement = storyElement.selectFirst("div.playlists-ajax");
        if (idElement != null) {
            String elementId = idElement.attr("data-news_id");
            if (StringUtil.isNumeric(elementId)) {
                id = Integer.parseInt(elementId);
            }
        }

        //	#dle-content > article > div:nth-child(1) > div.story_c
        Element storyPostElement = storyElement.selectFirst("div.story_c_left");

        String urlPoster = ParserUtils.getImageUrl(storyPostElement);

        Element ageElement = storyPostElement.selectFirst("span.story_age");
        if (ageElement != null) {
            String age = storyPostElement.getElementsByTag("sup").first().text().trim();
            animeDetailsModel.setAge(age);
        }

        String title = storyElement.selectFirst("div.story_c h2").text();

        Element trailerPrewiewElement = storyElement.selectFirst("div.trailer_preview");
        ScreenshotModel trailerModel = null;

        if (trailerPrewiewElement != null) {
            String trailerPreviewUrl = ParserUtils.getImageUrl(trailerPrewiewElement);
            String trailerUrl = buildTrailerUrl(trailerPreviewUrl);
            trailerModel = new ScreenshotModel(trailerPreviewUrl, trailerUrl);
        }

        Element storyElementDetail = storyElement.selectFirst("div.story_c");

        parseDetails(storyElementDetail, animeDetailsModel);

        Element screenshotElement = storyElement.selectFirst("#story_screen_list1");
        if (screenshotElement != null) {
            parseSreenshotsBlock(screenshotElement, animeDetailsModel);
        }

        animeDetailsModel.setRating(ParserUtils.parseRatingBlock(storyElement));

        // витягування схожих аніме
        // body > div.content > div:nth-child(10) > div > ul
        // #dle-content > article > div:nth-child(8) > div > ul
        Element similarsElement = doc.selectFirst("div > ul.portfolio_items");
        if (similarsElement != null) {
            parseSimilarBlock(similarsElement, animeDetailsModel);
        }

        animeDetailsModel.setId(id);
        animeDetailsModel.setAnimeUrl(doc.head().selectFirst("link[rel=canonical]").attr("href"));
        animeDetailsModel.setTitle(title);
        animeDetailsModel.setPosterUrl(urlPoster);
        animeDetailsModel.setTrailerModel(trailerModel);

        loadSate.postValue(LoadState.DONE);
        detailsModel.postValue(animeDetailsModel);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(animeDetailsModel);
        Log.i(TAG, json);
    }

    private void parseDetails(Element storyElements, AnimeDetailsModel model) {

        // витягування оригінальної назви
        Element originalTitleElement =
                storyElements.selectFirst("div.story_c_r strong:contains(Оригінальна назва)");
        if (originalTitleElement != null) {
            String originalTitle = originalTitleElement.nextSibling().toString().trim();
            model.setOriginalTitle(originalTitle);
        }

        // витягування року випуску аніме
        Element yearElement = storyElements.selectFirst("div.story_c_r strong:contains(Рік)");
        if (yearElement != null) {
            SimpleModel year =
                    ParserUtils.buidlSimpleModel(yearElement.nextElementSibling().selectFirst("a"));
            model.setReleaseYear(year);
        }

        // витягування жанру
        Elements genreLinksElement =
                storyElements
                        .selectFirst("div.story_c_r strong:contains(Жанр:)")
                        .nextElementSiblings()
                        .select("a[href*=/anime/]");
        if (genreLinksElement != null) {
            List<SimpleModel> genre = ParserUtils.getSimpleDataFromElements(genreLinksElement);
            model.setGenres(genre);
        }

        // витягування режисера
        Element directorElement =
                storyElements.selectFirst("div.story_c_r strong:contains(Режисер)");
        if (directorElement != null) {
            String director = directorElement.nextSibling().toString().trim();
            model.setDirector(director);
        }

        // витягування студії
        Element studioElement =
                storyElements.select("div.story_c_r strong:contains(Студія)").first();
        if (studioElement != null) {
            String studio = studioElement.nextSibling().toString().trim();
            model.setStudio(studio);
        }

        // витягування кількості серій
        Element episodesElement =
                storyElements.select("div.story_c_r strong:contains(Серій)").first();
        if (episodesElement != null) {
            String episodes = episodesElement.nextSibling().toString().trim();
            model.setEpisodes(episodes);
        }

        // витягування перекладу
        Elements translationElement = storyElements.select("div.story_c_r a[href*=/translation/]");
        if (translationElement != null) {
            List<SimpleModel> translation =
                    ParserUtils.getSimpleDataFromElements(translationElement);
            model.setTranslators(translation);
        }

        // витягування озвучення
        Element voicedElement =
                storyElements.selectFirst("div.story_c_r strong:contains(Ролі озвучували)");
        if (voicedElement != null) {
            parseDubbersBlock(voicedElement, model);
        }

        // витягування опису
        Element descriptionElement = storyElements.selectFirst("div.my-text");
        if (descriptionElement != null) {
            String description = descriptionElement.text().trim();
            model.setDescription(description);
        }
    }

    private void parseDubbersBlock(Element voicedElement, AnimeDetailsModel model) {
        Elements voicedElements = voicedElement.nextElementSiblings();
        // Log.i(TAG, "print voicedElements");
        //  Log.i(TAG, voicedElements.html());
        Elements teamLists = voicedElements.select("span.team_list");
        if (teamLists != null && !teamLists.isEmpty()) {
            List<DubbersTeam> dubbersTeamList = new ArrayList<>(teamLists.size());

            // Log.i(TAG, "print teamLists");
            // Log.i(TAG, teamLists.html());
            for (Element teamList : teamLists) {

                Element teamListByTag = teamList.getElementsByTag("a").first();
                String teamName = teamListByTag.text();
                String teamUrl = teamListByTag.attr("href");
                // Log.i(TAG, teamName + " " + teamUrl);
                SimpleModel dubberTeam = new SimpleModel(teamName, teamUrl);
                Elements teams = teamList.select("a.teams");
                List<SimpleModel> dubbers = ParserUtils.getDataFromAttr(teams);

                DubbersTeam dubbersTeam = new DubbersTeam(dubberTeam, dubbers);
                dubbersTeamList.add(dubbersTeam);
            }
            model.setDubbersTeamList(dubbersTeamList);
        } else {
            List<SimpleModel> voiced = ParserUtils.getDataFromAttr(voicedElements);
            if (voiced != null) {
                model.setVoicers(voiced);
            }
        }
    }

    private void parseSreenshotsBlock(Element screenshotModel, AnimeDetailsModel model) {
        Elements elements = screenshotModel.getElementsByTag("a");
        List<ScreenshotModel> screenshotsList = new ArrayList<>(elements.size());
        for (Element element : elements) {
            String screenshotFullUrl = element.attr("href");
            String screenshotMiniUrl = ParserUtils.getImageUrl(element);
            ScreenshotModel screenshot = new ScreenshotModel(screenshotMiniUrl, screenshotFullUrl);
            screenshotsList.add(screenshot);
        }

        model.setScreenshotsModel(screenshotsList);
    }

    private void parseSimilarBlock(Element element, AnimeDetailsModel model) {
        Elements elements = element.getElementsByTag("li");
        List<BaseAnimeModel> similarsList = new ArrayList<>(elements.size());
        for (Element similar : elements) {
            String posterUrl = ParserUtils.getImageUrl(similar.selectFirst("div.sl_poster"));
            Element textElement = similar.selectFirst("div.text_content a");
            String title = textElement.text();
            String url = textElement.attr("href");
            BaseAnimeModel similarModel = new BaseAnimeModel(title, posterUrl, url);
            similarsList.add(similarModel);
        }

        model.setSimilarAnimeList(similarsList);
    }

    private void parseRelatedBlock(Element element) {

        // #dle-content > article > div:nth-child(1) > div:nth-child(9) > style:nth-child(14)
        // document.querySelector("#dle-content > article > div:nth-child(1) > div:nth-child(9)")
    }

    private String buildTrailerUrl(String previewUrl) {
        // Використовуємо регулярний вираз, щоб отримати ID відео
        Pattern pattern = Pattern.compile(".*\\/([a-zA-Z0-9_-]{11}).*");
        Matcher matcher = pattern.matcher(previewUrl);

        if (matcher.matches()) {
            String videoId = matcher.group(1);
            return String.format("https://www.youtube.com/watch?v=%s", videoId);
        } else {
            return null;
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.dispose();
    }
}
