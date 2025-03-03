package com.mrikso.anitube.app.network.response;

import com.google.gson.annotations.SerializedName;

public class AnitubeAnimeResponse {

    @SerializedName("end_date")
    private int endDate;

    @SerializedName("image")
    private String image;

    @SerializedName("year")
    private int year;

    @SerializedName("scored_by")
    private int scoredBy;

    @SerializedName("rating")
    private String rating;

    @SerializedName("episodes_total")
    private int episodesTotal;

    @SerializedName("source")
    private String source;

    @SerializedName("episodes_released")
    private int episodesReleased;

    @SerializedName("translated_ua")
    private boolean translatedUa;

    @SerializedName("score")
    private double score;

    @SerializedName("title_ja")
    private String titleJa;

    @SerializedName("media_type")
    private String mediaType;

    @SerializedName("data_type")
    private String dataType;

    @SerializedName("title_ua")
    private String titleUa;

    @SerializedName("season")
    private String season;

    @SerializedName("title_en")
    private String titleEn;

    @SerializedName("slug")
    private String slug;

    @SerializedName("status")
    private String status;

    @SerializedName("start_date")
    private int startDate;

    public int getEndDate() {
        return endDate;
    }

    public String getImage() {
        return image;
    }

    public int getYear() {
        return year;
    }

    public int getScoredBy() {
        return scoredBy;
    }

    public String getRating() {
        return rating;
    }

    public int getEpisodesTotal() {
        return episodesTotal;
    }

    public String getSource() {
        return source;
    }

    public int getEpisodesReleased() {
        return episodesReleased;
    }

    public boolean isTranslatedUa() {
        return translatedUa;
    }

    public double getScore() {
        return score;
    }

    public String getTitleJa() {
        return titleJa;
    }

    public String getMediaType() {
        return mediaType;
    }

    public String getDataType() {
        return dataType;
    }

    public String getTitleUa() {
        return titleUa;
    }

    public String getSeason() {
        return season;
    }

    public String getTitleEn() {
        return titleEn;
    }

    public String getSlug() {
        return slug;
    }

    public String getStatus() {
        return status;
    }

    public int getStartDate() {
        return startDate;
    }
}