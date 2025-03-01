package com.mrikso.anitube.app.model;

import androidx.annotation.NonNull;

import com.google.common.base.Objects;

public class AnimeMobileDetailsModel {
    private String ageRating;
    private SimpleModel animeType;
    private String animeUpdateStatus;

    public String getAgeRating() {
        return ageRating;
    }

    public void setAgeRating(String ageRating) {
        this.ageRating = ageRating;
    }

    public SimpleModel getAnimeType() {
        return animeType;
    }

    public void setAnimeType(SimpleModel animeType) {
        this.animeType = animeType;
    }

    public String getAnimeUpdateStatus() {
        return animeUpdateStatus;
    }

    public void setAnimeUpdateStatus(String animeUpdateStatus) {
        this.animeUpdateStatus = animeUpdateStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnimeMobileDetailsModel that = (AnimeMobileDetailsModel) o;
        return Objects.equal(ageRating, that.ageRating) && Objects.equal(animeType, that.animeType) && Objects.equal(animeUpdateStatus, that.animeUpdateStatus);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(ageRating, animeType, animeUpdateStatus);
    }

    @NonNull
    @Override
    public String toString() {
        return "AnimeMobileDetailsModel{" +
                "ageRating='" + ageRating + '\'' +
                ", animeType='" + animeType + '\'' +
                ", animeUpdateStatus='" + animeUpdateStatus + '\'' +
                '}';
    }
}
