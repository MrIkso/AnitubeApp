package com.mrikso.anitube.app.parser;

import com.mrikso.anitube.app.model.FranchiseModel;
import com.mrikso.anitube.app.utils.ParserUtils;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class FranchiseParser {
    private final String RELEASE_YEAR_PATTERN = "Рік випуску аніме:\\s*(\\d{4})";
    private final String SERIES_PATTERN = "Серій:\\s*(.*\\))";

    public List<FranchiseModel> parseFranshises(String currentAnime, Element root) {
        Elements franchisesElements = root.select("a");
        List<FranchiseModel> franchises = new ArrayList<>(franchisesElements.size());
        for (Element franchisesElement : franchisesElements) {
            String url = franchisesElement.attr("href");

            Element article = franchisesElement.selectFirst("article");

            String imageUrl = article.selectFirst("img").attr("src");
            String animeTitle = article.selectFirst(".news_r_h .link").text().trim();

            String content = article.selectFirst(".news_r").text();

            String releaseYear = ParserUtils.getMatcherResult(RELEASE_YEAR_PATTERN, content, 1);
            String series = ParserUtils.getMatcherResult(SERIES_PATTERN, content, 1);
            String description = article.selectFirst(".news_r_c").ownText();

            boolean isCurrent = url.contains(currentAnime);

            FranchiseModel franchise = new FranchiseModel(animeTitle, imageUrl, url, isCurrent);
            franchise.setReleaseYear(releaseYear);
            franchise.setEpisodes(series);
            franchises.add(franchise);
        }
        return franchises;
    }
}
