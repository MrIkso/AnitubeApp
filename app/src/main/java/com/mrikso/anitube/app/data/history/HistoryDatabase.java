package com.mrikso.anitube.app.data.history;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.mrikso.anitube.app.data.history.dao.HistoryDao;
import com.mrikso.anitube.app.data.history.dao.LastWatchedEpisodeDao;
import com.mrikso.anitube.app.data.history.enity.HistoryEnity;
import com.mrikso.anitube.app.data.history.enity.LastWatchedEpisodeEnity;

@Database(
        entities = {HistoryEnity.class, LastWatchedEpisodeEnity.class},
        version = 1,
        exportSchema = false)
public abstract class HistoryDatabase extends RoomDatabase {
    private static final Object LOCK = new Object();
    private static final String DATABASE_NAME = "watch_history.db";
    private static HistoryDatabase sInstance;

    public static HistoryDatabase getInstance(Context context) {
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = Room.databaseBuilder(
                                context.getApplicationContext(), HistoryDatabase.class, HistoryDatabase.DATABASE_NAME)
                        .build();
            }
        }
        return sInstance;
    }

    public abstract HistoryDao historyDao();

    public abstract LastWatchedEpisodeDao lastWatchedEpisodeDao();
}
