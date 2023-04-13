package com.mrikso.anitube.app.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.mrikso.anitube.app.data.model.AnimeRemoteKeys;
import java.util.List;

@Dao
public interface AnimeRemoteKeysDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<AnimeRemoteKeys> remoteKeys);
    
    @Query("SELECT * FROM anime_remote_keys WHERE animeId = :animeId")
    AnimeRemoteKeys remoteKeysByAnimeId(int animeId);
    
    @Query("DELETE FROM anime_remote_keys")
    void clearRemoteKeys();
}