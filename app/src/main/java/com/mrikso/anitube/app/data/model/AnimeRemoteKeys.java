package com.mrikso.anitube.app.data.model;

import androidx.room.PrimaryKey;
import androidx.room.Entity;

@Entity(tableName = "anime_remote_keys")
public class AnimeRemoteKeys {
    @PrimaryKey int animeId;
    int prevKey;
    int nextKey;

    public AnimeRemoteKeys(int animeId, int prevKey, int nextKey) {
        this.animeId = animeId;
        this.prevKey = prevKey;
        this.nextKey = nextKey;
    }

    public int getAnimeId() {
        return this.animeId;
    }

    public void setAnimeId(int animeId) {
        this.animeId = animeId;
    }

    public int getPrevKey() {
        return this.prevKey;
    }

    public void setPrevKey(int prevKey) {
        this.prevKey = prevKey;
    }

    public int getNextKey() {
        return this.nextKey;
    }

    public void setNextKey(int nextKey) {
        this.nextKey = nextKey;
    }
}
