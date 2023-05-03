package com.mrikso.anitube.app.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "anime_remote_keys")
public class AnimeRemoteKeys {
    @PrimaryKey int animeId;
    Integer prevKey;
    Integer nextKey;

    public AnimeRemoteKeys(int animeId, Integer prevKey, Integer nextKey) {
        this.animeId = animeId;
        this.prevKey = prevKey;
        this.nextKey = nextKey;
    }

    public int getAnimeId() {
        return this.animeId;
    }

    public Integer getPrevKey() {
        return this.prevKey;
    }

    public Integer getNextKey() {
        return this.nextKey;
    }
}
