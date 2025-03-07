package com.mrikso.anitube.app.network.response;

import com.google.gson.annotations.SerializedName;

public class Anime {

    @SerializedName("image")
    private String image;

    @SerializedName("score")
    private Double score;

    @SerializedName("title_ja")
    private String titleJa;

    @SerializedName("media_type")
    private String mediaType;

    @SerializedName("data_type")
    private String dataType;

    @SerializedName("scored_by")
    private int scoredBy;

    @SerializedName("title_ua")
    private String titleUa;

    @SerializedName("title_en")
    private String titleEn;

    @SerializedName("episodes_total")
    private int episodesTotal;

    @SerializedName("slug")
    private String slug;

    @SerializedName("episodes_released")
    private int episodesReleased;

    @SerializedName("status")
    private String status;

    public String getImage() {
        return image;
    }

    public Double getScore() {
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

    public int getScoredBy() {
        return scoredBy;
    }

    public String getTitleUa() {
        return titleUa;
    }

    public String getTitleEn() {
        return titleEn;
    }

    public int getEpisodesTotal() {
        return episodesTotal;
    }

    public String getSlug() {
        return slug;
    }

    public int getEpisodesReleased() {
        return episodesReleased;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "Anime{" +
                "image='" + image + '\'' +
                ", score=" + score +
                ", titleJa='" + titleJa + '\'' +
                ", mediaType='" + mediaType + '\'' +
                ", dataType='" + dataType + '\'' +
                ", scoredBy=" + scoredBy +
                ", titleUa='" + titleUa + '\'' +
                ", titleEn='" + titleEn + '\'' +
                ", episodesTotal=" + episodesTotal +
                ", slug='" + slug + '\'' +
                ", episodesReleased=" + episodesReleased +
                ", status='" + status + '\'' +
                '}';
    }
}