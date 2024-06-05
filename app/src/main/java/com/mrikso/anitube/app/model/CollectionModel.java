package com.mrikso.anitube.app.model;

import com.google.common.base.Objects;

import java.io.Serializable;

public class CollectionModel implements Serializable {
    private int countAnime;
    private String nameCollection;
    private String collectionUrl;
    private String posterUrl;
    private String author;

    public int getCountAnime() {
        return this.countAnime;
    }

    public void setCountAnime(int countAnime) {
        this.countAnime = countAnime;
    }

    public String getNameCollection() {
        return this.nameCollection;
    }

    public void setNameCollection(String nameCollection) {
        this.nameCollection = nameCollection;
    }

    public String getCollectionUrl() {
        return this.collectionUrl;
    }

    public void setCollectionUrl(String collectionUrl) {
        this.collectionUrl = collectionUrl;
    }

    public String getPosterUrl() {
        return this.posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public String getAuthor() {
        return this.author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    @Override
    public String toString() {
        return "CollectionModel[countAnime="
                + countAnime
                + ", nameCollection="
                + nameCollection
                + ", collectionUrl="
                + collectionUrl
                + ", posterUrl="
                + posterUrl
                + ", author="
                + author
                + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CollectionModel that = (CollectionModel) o;
        return countAnime == that.countAnime && Objects.equal(nameCollection, that.nameCollection) && Objects.equal(collectionUrl, that.collectionUrl) && Objects.equal(posterUrl, that.posterUrl) && Objects.equal(author, that.author);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(countAnime, nameCollection, collectionUrl, posterUrl, author);
    }
}
