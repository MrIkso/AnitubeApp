package com.mrikso.anitube.app.data.search;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public interface SearchDao {
    @Query("SELECT * FROM recent_searches")
    LiveData<List<RecentSearch>> getAllRecentSearch();

    @Query("SELECT * FROM recent_searches WHERE search_name = :name")
    RecentSearch getSearchesByName(String name);

    @Query("DELETE FROM recent_searches WHERE search_name = :name")
    void deleteSearchesByName(String name);

    @Insert(onConflict = REPLACE)
    void insertSearch(RecentSearch recentSearch);

    @Query("DELETE FROM recent_searches")
    void clearSearches();
}
