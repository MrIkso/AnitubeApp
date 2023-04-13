package com.mrikso.anitube.app.data.dao;

import androidx.paging.PagingSource;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.mrikso.anitube.app.model.AnimeReleaseModel;
import io.reactivex.rxjava3.core.Flowable;
import java.util.List;

@Dao
public interface AnimeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<AnimeReleaseModel> movies);

    @Query("SELECT * FROM animes ORDER BY id ASC")
    Flowable<PagingSource<Integer, AnimeReleaseModel>> selectAll();

    @Query("DELETE FROM animes")
    void clearMovies();
}
