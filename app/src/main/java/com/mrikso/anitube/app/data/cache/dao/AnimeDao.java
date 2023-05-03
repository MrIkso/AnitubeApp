package com.mrikso.anitube.app.data.dao;

import androidx.room.Dao;

@Dao
public interface AnimeDao {

    // @Insert(onConflict = OnConflictStrategy.REPLACE)
    // void insertAll(List<AnimeReleaseModel> movies);

    // @Query("SELECT * FROM animes ORDER BY id ASC")
    //  PagingSource<Integer, AnimeReleaseModel> selectAll();

    //  @Query("DELETE FROM animes")
    //  void clearAnimes();
}
