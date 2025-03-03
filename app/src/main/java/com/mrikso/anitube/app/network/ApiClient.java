package com.mrikso.anitube.app.network;

public class ApiClient {
    public static final String DESKTOP_USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/113.0.0.0 Safari/537.36";
    public static final String MOBILE_USER_AGENT =
            "Mozilla/5.0 (Linux; Android 13; SM-M526B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.5615.135 Mobile Safari/537.36 OPR/75.2.3978.72468";

    public static final String BASE_URL = "https://anitube.in.ua";
    public static final String RANDOM_ANIME_URl = BASE_URL + "/?do=random_anime";
    public static final String PROFILE_BG_URL = "https://anitube.in.ua/templates/smartphone/images/profile_bg.jpg";
    public static final String ANIME_CAROUSEL_BG_URL =
            "https://anitube.in.ua/templates/AniTubenew/images/images/slider/slayd2.png";
    public static final String ANIME_CALLENDAR_URL =
            "https://mrikso.bitbucket.io/AnitubeApp/images/callendar_anime.jpg";
    public static final String ANIME_GENRES_URL = "https://mrikso.bitbucket.io/AnitubeApp/images/genres_anime.jpg";
    public static final String ANIME_RANDOM_BG_URL = "https://mrikso.bitbucket.io/AnitubeApp/images/random_anime.jpg";
    public static final String SIGN_UP_URL = BASE_URL +"/index.php?do=register";
    public static final String LOSTPASSWORD_URL = BASE_URL + "/index.php?do=lostpassword";
    public static final String HIKKA_URL = "https://hikka.io";
    public static final String HIKKA_API_URL = "https://api.hikka.io";
}
