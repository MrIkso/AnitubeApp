package com.mrikso.anitube.app.parser;

import com.mrikso.anitube.app.model.FranchiseModel;
import com.mrikso.anitube.app.utils.ParserUtils;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class FranchiseParser {
    private final String RELEASE_YEAR_PATTERN = "Рік випуску аніме:\\s*(\\d{4})";
    private final String SERIES_PATTERN = "Серій:\\s*(.*)";

    public List<FranchiseModel> parseFranshises(String currentAnime, Element root) {
        Elements franchisesElements = root.select("a");
        List<FranchiseModel> franchises = new ArrayList<>(franchisesElements.size());
        for (Element franchisesElement : franchisesElements) {
            String animeUrl = franchisesElement.attr("href");

            Element article = franchisesElement.selectFirst("article");

            String imageUrl = article.selectFirst("img").attr("src");
            String animeTitle = article.selectFirst(".news_r_h .link").text().trim();

            List<Node> content = article.selectFirst(".news_r").childNodes();

            String releaseYear = ParserUtils.getMatcherResult(
                    RELEASE_YEAR_PATTERN, content.get(2).toString(), 1);
            String series =
                    ParserUtils.getMatcherResult(SERIES_PATTERN, content.get(4).toString(), 1);
            String description = article.selectFirst(".news_r_c").ownText();

            boolean isCurrent = animeUrl.contains(currentAnime);
            int animeId = ParserUtils.getAnimeId(animeUrl);

            FranchiseModel franchise = new FranchiseModel(animeId, animeTitle, imageUrl, animeUrl, isCurrent);
            franchise.setReleaseYear(releaseYear);
            franchise.setEpisodes(series);
            franchises.add(franchise);
        }
        return franchises;
    }
}
