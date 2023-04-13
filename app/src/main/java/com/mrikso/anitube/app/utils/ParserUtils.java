package com.mrikso.anitube.app.utils;

import com.mrikso.anitube.app.model.SimpleModel;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParserUtils {

    public static List<SimpleModel> getDataFromAttr(Element el) {
        Elements elements = el.getElementsByTag("a");
        List<SimpleModel> dataList = new ArrayList<>();
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
        return element.getElementsByTag("img").first().attr("src");
    }

    public static SimpleModel buidlSimpleModel(Element element) {
        return new SimpleModel(element.text().trim(), element.attr("href"));
    }

    public static String parseRatingBlock(Element element) {
        Element rateElement =
                element.selectFirst("div.story_c_rate > div.lexington-box > div > span");
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

    public static String getMatcherResult(String regex, String content, int group) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(content);
        boolean found = matcher.find();
        if (found) return matcher.group(group);
        else return null;
    }
}
