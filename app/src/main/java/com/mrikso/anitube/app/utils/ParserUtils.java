package com.mrikso.anitube.app.utils;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.base.Strings;
import com.mrikso.anitube.app.R;
import com.mrikso.anitube.app.model.SimpleModel;
import com.mrikso.anitube.app.model.WatchAnimeStatusModel;
import com.mrikso.anitube.app.network.ApiClient;
import com.mrikso.anitube.app.parser.DleHashParser;
import com.mrikso.anitube.app.repository.ViewStatusAnime;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParserUtils {

    public static List<SimpleModel> getDataFromAttr(Element el) {
        Elements elements = el.getElementsByTag("a");
        List<SimpleModel> dataList = new ArrayList<>(elements.size());
        for (Element element : elements) {
            dataList.add(buidlSimpleModel(element));
        }
        return dataList;
    }

    public static List<SimpleModel> getDataFromAttr(Elements el) {
        List<SimpleModel> dataList = new ArrayList<>();
        for (Element element : el) {
            Elements elements = element.getElementsByTag("a");
            for (Element tagElement : elements) {
                dataList.add(buidlSimpleModel(tagElement));
            }
        }

        return dataList;
    }

    public static List<SimpleModel> getSimpleDataFromElements(Elements el) {
        //	Log.i("parser", el.html());
        List<SimpleModel> dataList = new ArrayList<>();
        for (Element element : el) {
            dataList.add(buidlSimpleModel(element));
        }

        return dataList;
    }

    public static String getImageUrl(Element element) {
        Element image = element.getElementsByTag("img").first();
        String imagePath = image.attr("src");
        if(imagePath.isEmpty()){
            return image.attr("data-src");
        }
        return imagePath;
    }

    public static SimpleModel buidlSimpleModel(Element element) {
        // Log.i("ParserUtils", element.text().trim() + " " + element.attr("href"));
        return new SimpleModel(element.text().trim(), element.attr("href"));
    }

    public static String parseRatingBlock(Element element) {
        Element rateElement = element.selectFirst("div.story_c_rate > div.lexington-box")
                .getElementsByTag("span")
                .first();
        if (rateElement != null) {
            String rate = rateElement.text();
            return rate;
        }
        return null;
    }

    public static int countMatches(String text, String find) {
        int index = 0, count = 0, length = find.length();
        while ((index = text.indexOf(find, index)) != -1) {
            index += length;
            count++;
        }
        return count;
    }

    @Nullable
    public static String getMatcherResult(@NonNull String regex, @NonNull String content, int group) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(content);
        boolean found = matcher.find();
        return found ? matcher.group(group) : null;
    }

    public static String standardizeQuality(String rawQuality) {
        if (Strings.isNullOrEmpty(rawQuality)) {
            return null;
        }
        if (!rawQuality.endsWith("p")) {
            return rawQuality + "p";
        }
        return rawQuality;
    }

    public static String normaliseImageUrl(String rawUrl) {
        //Log.i("ParserUtils", "rawUrl:" + rawUrl);
        if (rawUrl.startsWith(ApiClient.BASE_URL)) {
            return rawUrl;
        } else if (rawUrl.startsWith("//")) {
            return "https:" + rawUrl;
        } else if (rawUrl.startsWith("/")) {
            return ApiClient.BASE_URL + rawUrl;
        }
        return ApiClient.BASE_URL + "/" + rawUrl;
    }

    public static void parseDleHash(String data) {
        Executors.newSingleThreadExecutor().execute(() -> {
            String hash = DleHashParser.getHash(data);
            PreferencesHelper.getInstance().setDleHash(hash);
        });
    }

    public static boolean isAnimeLink(String url) {
        // перевіряємо, що посилання має "/<цифри>-<текст>"
        String[] urlParts = url.split("/");
        if (urlParts.length < 4) {
            return false;
        }
        String lastPart = urlParts[urlParts.length - 1];
        if (lastPart.matches("\\d+-\\S+\\.html")) {
            return true;
        }

        return false;
    }

    public static int getAnimeId(String url) {
        String[] parts = url.split("-");
        int id = Integer.parseInt(parts[0].substring(parts[0].lastIndexOf("/") + 1));
        return id;
    }

    public static WatchAnimeStatusModel getWatchModel(String status) {
        if (Strings.isNullOrEmpty(status)) {
            return null;
        }
        WatchAnimeStatusModel watchModel = new WatchAnimeStatusModel();
        watchModel.setStatus(status);
        if (status.equalsIgnoreCase("Переглянуто")) {
            watchModel.setViewStatus(ViewStatusAnime.STATUS_SEEN);
            watchModel.setColor(R.color.anime_status_completed);
        }
        if (status.equalsIgnoreCase("Заплановано")) {
            watchModel.setViewStatus(ViewStatusAnime.STATUS_WILL);
            watchModel.setColor(R.color.anime_status_plan_to_watch);
        }
        if (status.equalsIgnoreCase("Переглядаю")) {
            watchModel.setViewStatus(ViewStatusAnime.STATUS_WATCH);
            watchModel.setColor(R.color.anime_status_watching);
        }
        if (status.equalsIgnoreCase("Відкладено")) {
            watchModel.setViewStatus(ViewStatusAnime.STATUS_PONED);
            watchModel.setColor(R.color.anime_status_postponed);
        }
        if (status.equalsIgnoreCase("Покинуто")) {
            watchModel.setViewStatus(ViewStatusAnime.STATUS_ADAND);
            watchModel.setColor(R.color.anime_status_dropped);
        }

        return watchModel;
    }

    public static WatchAnimeStatusModel getWatchModel(int status) {

        WatchAnimeStatusModel watchModel = new WatchAnimeStatusModel();
        switch (status) {
            case 4:
                watchModel.setStatus("Переглянуто");
                watchModel.setViewStatus(ViewStatusAnime.STATUS_SEEN);
                watchModel.setColor(R.color.anime_status_completed);
                return watchModel;
            case 2:
                watchModel.setStatus("Заплановано");
                watchModel.setViewStatus(ViewStatusAnime.STATUS_WILL);
                watchModel.setColor(R.color.anime_status_plan_to_watch);
                return watchModel;
            case 3:
                watchModel.setStatus("Переглядаю");
                watchModel.setViewStatus(ViewStatusAnime.STATUS_WATCH);
                watchModel.setColor(R.color.anime_status_watching);
                return watchModel;
            case 5:
                watchModel.setStatus("Відкладено");
                watchModel.setViewStatus(ViewStatusAnime.STATUS_PONED);
                watchModel.setColor(R.color.anime_status_postponed);
                return watchModel;
            case 6:
                watchModel.setStatus("Покинуто");
                watchModel.setViewStatus(ViewStatusAnime.STATUS_ADAND);
                watchModel.setColor(R.color.anime_status_dropped);
                return watchModel;
        }
        return null;
    }
}
