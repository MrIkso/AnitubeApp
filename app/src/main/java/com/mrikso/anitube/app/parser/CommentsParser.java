package com.mrikso.anitube.app.parser;

import android.util.Log;

import com.google.common.base.Strings;
import com.mrikso.anitube.app.model.CommentModel;
import com.mrikso.anitube.app.utils.ParserUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommentsParser {
    public CommentsParser() {}

    public List<CommentModel> transform(String data) {
        if (Strings.isNullOrEmpty(data)) {
            return Collections.emptyList();
        }
        Document doc = Jsoup.parse(data);
        Elements commentsElement = doc.select("div[id^=comment-id-]");
        List<CommentModel> commentsList = new ArrayList<>(commentsElement.size());
        for (Element commentElement : commentsElement) {
            CommentModel model = new CommentModel();
            String commentId = commentElement.attr("id");
            String[] parts = commentId.split("-");
            if (parts.length > 2) {
                int id = Integer.parseInt(parts[2]);
                model.setCommentId(id);
            }
            Element commentAuthorElement = commentElement.selectFirst("div.comments__autor");
            String userAva = ParserUtils.getImageUrl(commentAuthorElement);

            Element commentsNameElememt = commentAuthorElement.selectFirst("span.comments__name");
            Element commentUser = commentsNameElememt.selectFirst("a");
            String username = commentUser.text();
            String userLink = commentUser.attr("href");

            Elements commentsTimeElement = commentAuthorElement.select("span.comments__time");
            String userGroup = commentsTimeElement.get(0).text();
            String commentTime = commentsTimeElement.get(1).text();

            // #comm-id-45753 > div.title_quote
            // #comment-id-45753 > div > div.comm-item.clearfix > div > div
            Element contentElement = commentElement.selectFirst("div.comm-item.clearfix > div.comm-right");
            Element titleQuoteElement = contentElement.selectFirst("div.title_quote");
            if (titleQuoteElement != null) {
                String titleQuote = titleQuoteElement.text().trim();
            }

            Element quoteElement = contentElement.selectFirst("div.quote");
            if (quoteElement != null) {
                String quote = quoteElement.html();
            }
            Log.i("commparser", commentId);
            String content = contentElement
                    .getElementById("comm-id-" + model.getCommentId())
                    .html();

            model.setUsername(username);
            model.setUserAvarar(userAva);
            model.setUserGroup(userGroup);
            model.setUserLink(userLink);
            model.setTime(commentTime);
            model.setContent(content);

            commentsList.add(model);
        }
        return commentsList;
    }
}
