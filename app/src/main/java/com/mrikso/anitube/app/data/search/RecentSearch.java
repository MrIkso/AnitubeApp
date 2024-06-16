package com.mrikso.anitube.app.data.search;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.common.base.Objects;

@Entity(tableName = "recent_searches")
public class RecentSearch {
    @PrimaryKey(autoGenerate = true)
    private int search_id;

    @ColumnInfo(name = "search_name")
    private String search_name;

    public RecentSearch(int search_id, String search_name) {
        this.search_id = search_id;
        this.search_name = search_name;
    }

    @Ignore
    public RecentSearch(String search_name) {
        this.search_name = search_name;
    }

    public int getSearch_id() {
        return search_id;
    }

    public void setSearch_id(int search_id) {
        this.search_id = search_id;
    }

    public String getSearch_name() {
        return search_name;
    }

    public void setSearch_name(String search_name) {
        this.search_name = search_name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecentSearch that = (RecentSearch) o;
        return search_id == that.search_id && Objects.equal(search_name, that.search_name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(search_id, search_name);
    }
}
