package com.mrikso.anitube.app.parser;

import com.google.common.base.Strings;
import com.mrikso.anitube.app.model.UserProfileModel;
import com.mrikso.anitube.app.network.ApiClient;
import com.mrikso.anitube.app.utils.ParserUtils;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class UserProfileParser {
    private final Document doc;

    public UserProfileParser(Document doc) {
        this.doc = doc;
        HomePageParser.getInstance().parseUserData(doc);
    }

    public UserProfileModel getUserProfile() {
        // #userinfo > div.lcol > div > div
        // #dle-content > div.lcol > div > h1 > span:nth-child(1)
        UserProfileModel model = new UserProfileModel();
        Element userinfoElenment = doc.selectFirst("#dle-content");

        String profileCardStyle = userinfoElenment.selectFirst("div.lcol > div.box").attr("style");
        if (Strings.isNullOrEmpty(profileCardStyle)) {
            model.setProfileBackground(ApiClient.PROFILE_BG_URL);
        } else {
            String profileCardBg = ParserUtils.getMatcherResult("background:url\\((.+)\\)", profileCardStyle, 1);
            model.setProfileBackground(Strings.isNullOrEmpty(profileCardBg) ? ApiClient.PROFILE_BG_URL : ParserUtils.normaliseImageUrl(profileCardBg));
        }

        String username = userinfoElenment.selectFirst("h1 span").text().trim();
        model.setUsername(username);

        Element userDetails = userinfoElenment.selectFirst("div.user_info");
        String avatar = userDetails.selectFirst("div.avatar > span > img").attr("src");
        model.setUserAvatarUrl(avatar);

        Element onlineElement = userDetails.selectFirst("div.user_info_l span a");
        if (onlineElement != null) {
            String online = onlineElement.text().trim();
            model.setUserOnline(online);
        }

        Element groupElement = userDetails.selectFirst("div.user_info_r strong:contains(Група)");
        if (groupElement != null) {
            String group;
            Element span = groupElement.nextElementSibling();
            if (span != null) {
                group = span.text().trim().replaceAll("\"", "");
            } else {
                group = groupElement.nextSibling().toString().trim().replaceAll("\"", "");
            }
            model.setUserGroup(group);
        }
        Element nameElement = userDetails.selectFirst("div.user_info_r strong:contains(Ім\\'я)");
        if (nameElement != null) {
            String name = nameElement.nextSibling().toString().trim().replaceAll("\"", "");
            model.setName(name);
        }

        Element registerElement = userDetails.selectFirst("div.user_info_r strong:contains(Реєстрація)");
        if (registerElement != null) {
            String register = registerElement.nextSibling().toString().trim().replaceAll("\"", "");
            model.setUserRegisterData(register);
        }

        Element lastActivityElement = userDetails.selectFirst("div.user_info_r strong:contains(Відвідини)");
        if (lastActivityElement != null) {
            String lastActivity =
                    lastActivityElement.nextSibling().toString().trim().replaceAll("\"", "");
            model.setUserLastActivityData(lastActivity);
        }

        Element signElement = userDetails.selectFirst("div.user_info_r strong:contains(Підпис)");
        if (signElement != null) {
            String sign = signElement.nextSibling().toString().trim().replaceAll("\"", "");
            model.setUserStatus(sign);
        }

        Element infoElement = userDetails.selectFirst("div.user_info_r strong:contains(Про себе)");
        if (infoElement != null) {
            String info = infoElement.nextSibling().toString().trim().replaceAll("\"", "");
            model.setUserInfo(info);
        }

        Element cityElement = userDetails.selectFirst("div.user_info_r strong:contains(Рідне місто)");
        if (cityElement != null) {
            String city = cityElement.nextSibling().toString().trim().replaceAll("\"", "");
            model.setUserCity(city);
        }

        Element commentsElement = userDetails
                .selectFirst("div.user_info_r strong:contains(Коментарів)")
                .nextElementSiblings()
                .first();
        if (commentsElement != null) {
            int comments = Integer.parseInt(commentsElement.text().strip().replaceAll("\"", "").replaceAll("\\s", ""));
            model.setUserCommentsCount(comments);
        }

        /*Element commentsRatingElement = userDetails
                .selectFirst("div.user_info_r strong:contains(Рейтинг коментарів)")
                .nextElementSiblings()
                .first();

        if (commentsRatingElement != null) {
            String rating = commentsRatingElement.text().trim().replaceAll("\"", "");
            model.setUserCommentsRating(rating);
        }*/

        //Log.i("userparser", userDetails.html());

        return model;
    }
}
