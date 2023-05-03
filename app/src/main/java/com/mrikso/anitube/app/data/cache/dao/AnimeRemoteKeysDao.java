package com.mrikso.anitube.app.data.dao;

import androidx.room.Dao;

@Dao
public interface AnimeRemoteKeysDao {

    // @Insert(onConflict = OnConflictStrategy.REPLACE)
    //  void insertAll(List<AnimeRemoteKeys> remoteKeys);

    //  @Query("SELECT * FROM anime_remote_keys WHERE animeId = :animeId")
    //  AnimeRemoteKeys remoteKeysByAnimeId(int animeId);

    //  @Query("DELETE FROM anime_remote_keys")
    //  void clearRemoteKeys();
}
