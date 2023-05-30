package com.mrikso.anitube.app.data.history.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.mrikso.anitube.app.data.history.enity.HistoryEnity;

import java.util.List;

@Dao
public interface HistoryDao {
    @Query("SELECT * FROM watch_history ORDER BY watch_date DESC")
    LiveData<List<HistoryEnity>> getAllHistory();

    @Query("SELECT * FROM watch_history WHERE anime_id = :id")
    HistoryEnity getById(int id);

    @Query("DELETE FROM watch_history WHERE anime_id = :id")
    void deleteById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(HistoryEnity recentSearch);

    @Query("DELETE FROM watch_history")
    void deleteAll();

    @Update
    void update(HistoryEnity item);
}
